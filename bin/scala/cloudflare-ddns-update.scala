#!/usr/bin/env -S scala shebang

//> using scala "3.5.1"
//> using toolkit default
//> using dep "com.monovore::decline:2.4.1"
//> using options -java-output-version:17
//> using packaging.graalvmArgs --no-fallback --initialize-at-build-time -Os

import cats.syntax.all.given
import com.monovore.decline.*
import sttp.client4.quick.*
import sttp.client4.Response
import upickle.default.*
import java.net.InetAddress
import scala.util.boundary
import scala.util.boundary.break

case class Args(
    zone: String,
    record: String,
    cloudflareAuthEmail: String,
    cloudflareAuthKey: String,
    debug: Boolean
)

enum IpVersion:
    case V4, V6

    def default: String = this match
        case IpVersion.V4 => "0.0.0.0"
        case IpVersion.V6 => "0:0:0:0:0:0:0:0"

case class QueryResponse(
    result: List[QueryItem]
) derives ReadWriter

case class QueryItem(
    id: String,
    name: String,
) derives ReadWriter

case class UpdateRequest(
    `type`: String,
    name: String,
    content: String,
    ttl: Int,
    proxied: Boolean
) derives ReadWriter

extension [T](resp: Response[T])
    def check(label: String, debug: Boolean = false): Response[T] =
        if !resp.code.isSuccess then
            System.err.println(s"ERROR: Failure on $label — HTTP ${resp.code.code}")
            System.exit(1)
        else if debug then
            println(s"DEBUG: Success on $label — HTTP ${resp.code.code}")
        resp

def fetchPublicIp(version: IpVersion, debug: Boolean): Option[String] =
    for 
        ip <- dnsResolve("checkip.amazonaws.com", version, debug).headOption
        body = quickRequest.get(uri"http://$ip")
            .send()
            .check(s"fetchPublicIp($version)")
            .body
        nonEmpty <- Option(body.trim).filter(_.nonEmpty)
    yield nonEmpty

def dnsResolve(hostname: String, version: IpVersion | Null, debug: Boolean) =
    try 
        InetAddress.getAllByName(hostname)
            .filter: addr =>
                version match
                    case IpVersion.V4 => addr.getAddress.length == 4
                    case IpVersion.V6 => addr.getAddress.length > 4
                    case null => true
            .map(_.getHostAddress.trim)
            .toList
    catch 
        case e: java.net.UnknownHostException => 
            if debug then
                System.err.println(s"WARN: DNS resolution failed for $hostname")
            Nil

def getZoneId(args: Args) =
    val url = s"https://api.cloudflare.com/client/v4/zones?name=${args.zone}&status=active"
    if args.debug then
        println(s"DEBUG: GET $url")
    val resp = quickRequest.get(uri"$url")
        .header("X-Auth-Email", args.cloudflareAuthEmail)
        .header("X-Auth-Key", args.cloudflareAuthKey)
        .send()
        .check("getZoneId", args.debug)
    read[QueryResponse](resp.body)
        .result
        .find(_.name.contains(args.zone))
        .map(_.id)
    
def getRecordId(args: Args, zoneId: String, version: IpVersion) = 
    val recordType = version match
        case IpVersion.V4 => "A"
        case IpVersion.V6 => "AAAA"
    val url = s"https://api.cloudflare.com/client/v4/zones/$zoneId/dns_records?type=${recordType}&name=${args.record}"
    if args.debug then
        println(s"DEBUG: GET $url")
    val resp = quickRequest.get(uri"$url")
        .header("X-Auth-Email", args.cloudflareAuthEmail)
        .header("X-Auth-Key", args.cloudflareAuthKey)
        .send()
        .check("recordId", args.debug)
    read[QueryResponse](resp.body.toString)
        .result
        .find(_.name.contains(args.zone))
        .map(_.id)

def updateRecord(args: Args, zoneId: String, recordId: Option[String], version: IpVersion, ip: String) =
    val initRequest = recordId match
        case Some(recordId) => 
            val url = s"https://api.cloudflare.com/client/v4/zones/$zoneId/dns_records/$recordId"
            if args.debug then println(s"DEBUG: PUT $url")
            quickRequest.put(uri"$url")
        case None =>
            val url = s"https://api.cloudflare.com/client/v4/zones/$zoneId/dns_records"
            if args.debug then println(s"DEBUG: POST $url")
            quickRequest.post(uri"$url")

    val recordType = version match
        case IpVersion.V4 => "A"
        case IpVersion.V6 => "AAAA"
    val updateRequest = UpdateRequest(
        `type` = recordType,
        name = args.record,
        content = ip,
        ttl = 1,
        proxied = false
    )

    initRequest
        .header("X-Auth-Email", args.cloudflareAuthEmail)
        .header("X-Auth-Key", args.cloudflareAuthKey)
        .body(write(updateRequest))
        .send()
        .check("updateRecord", args.debug)
    ()

def update(args: Args, version: IpVersion): Either[String, Unit] = boundary:
    val publicIp = fetchPublicIp(version, args.debug).getOrElse(version.default)
    println(s"Public IP $version: $publicIp")

    dnsResolve(args.record, version, args.debug) match
        case `publicIp` :: Nil =>
            if args.debug then 
                println(s"DNS record already up-to-date for IP $version: $publicIp")
            break(Right(()))
        case Nil =>
            println(s"DNS record not found for IP $version: $publicIp")
        case list =>
            println(s"Current IP address does not match DNS record, updating: ${list.mkString(", ")} -> $publicIp")

    val zoneId = getZoneId(args).getOrElse:
        break(Left(s"Failed to find zone ID for ${args.zone}"))
    
    val recordId = getRecordId(args, zoneId, version)
    updateRecord(args, zoneId, recordId, version, publicIp)
    Right(())

def run(args: Args): Unit =
    System.setProperty("jdk.httpclient.allowRestrictedHeaders", "Host");

    update(args, IpVersion.V4).valueOr: err =>
        System.err.println(s"ERROR: $err")
        System.exit(1)
    update(args, IpVersion.V6).valueOr: err =>
        System.err.println(s"ERROR: $err")
        System.exit(1)

object Main extends CommandApp(
    name = "cloudflare-ddns-update",
    header = "Update Cloudflare DNS record with current public IP address",
    main =
        val zone = Opts
            .option[String]("zone", help = "Cloudflare Zone ID (e.g., the domain's name)")
            .orElse(Opts.env[String]("CLOUDFLARE_DDNS_ZONE", help = "Cloudflare Zone ID (e.g., the domain's name)"))
        val record = Opts
            .option[String]("record", help = "Cloudflare Record ID (e.g., full hostname)")
            .orElse(Opts.env[String]("CLOUDFLARE_DDNS_RECORD", help = "Cloudflare Record ID (e.g., full hostname)"))
        val cloudflareAuthEmail = Opts
            .option[String]("auth-email", help = "Cloudflare account email")
            .orElse(Opts.env[String]("CLOUDFLARE_AUTH_EMAIL", help = "Cloudflare account email"))
        val cloudflareAuthKey = Opts
            .option[String]("auth-key", help = "Cloudflare API key")
            .orElse(Opts.env[String]("CLOUDFLARE_AUTH_KEY", help = "Cloudflare API key"))
        val debug = Opts
            .flag("debug", help = "Enable verbose output").orFalse
        
        (zone, record, cloudflareAuthEmail, cloudflareAuthKey, debug)
            .mapN(Args.apply)
            .map(run)
)
