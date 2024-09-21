#!/usr/bin/env -S scala shebang

//> using scala "3.5.1"
//> using toolkit default
//> using dep "com.monovore::decline:2.4.1"
//> using options -java-output-version:17
//> using packaging.graalvmArgs --no-fallback --initialize-at-build-time

import cats.syntax.all.given
import com.monovore.decline.*
import sttp.client4.quick.*
import sttp.client4.Response
import upickle.default.*
import java.net.InetAddress
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.nio.file.Path

extension [T](resp: Response[T])
    def check(label: String, debug: Boolean = false): Unit =
        if !resp.code.isSuccess then
            System.err.println(s"ERROR: Failure on $label — HTTP ${resp.code.code}")
            System.exit(1)
        else if debug then
            println(s"DEBUG: Success on $label — HTTP ${resp.code.code}")

case class Host(name: String, ips: List[String])

def parseHostsFile(path: Path): List[Host] =
    scala.io.Source.fromFile(path.toFile)
        .getLines
        .map(_.trim)
        .filterNot(l => l.isEmpty || l.startsWith("#"))
        .map(_.split("\\s+").toList)
        .collect:
            case name :: ips => Host(name, ips)
        .toList
        .groupBy(_._1)
        .map: 
            case (name, ips) =>
                Host(name, ips.toList.map(_._2).flatten)
        .toList

def run(hosts: List[Host], apiKey: String, profileId: String, dryRun: Boolean, verbose: Boolean) =
    case class GetResponse(data: List[Map[String, String]])
        derives ReadWriter

    lazy val printHeaderOnce: Unit =
        val now = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z"))
        println(s"---")
        if dryRun then
            println(s"[$now] DRY RUN, no changes will be made...")
        else
            println(s"[$now] Updating NextDNS rewrites...")

    lazy val getProfile =
        val resp = quickRequest
            .get(uri"https://api.nextdns.io/profiles/$profileId/rewrites")
            .header("X-Api-Key", apiKey)
            .send()
        upickle.default.read[GetResponse](resp.body.toString)

    def post(name: String, ip: String) =
        printHeaderOnce
        println(s"Adding $name (ip: $ip)")
        if (!dryRun) then quickRequest
            .post(uri"https://api.nextdns.io/profiles/$profileId/rewrites")
            .header("X-Api-Key", apiKey)
            .header("Content-Type", "application/json")
            .body(upickle.default.write(
                Map(
                    "name" -> name,
                    "content" -> ip
                )
            ))
            .send()
            .check(s"POST $name:$ip")

    def delete(id: String, name: String, ip: String) =
        printHeaderOnce
        println(s"Deleting `$name` entry (ip: $ip)")
        if !dryRun then quickRequest
            .delete(uri"https://api.nextdns.io/profiles/$profileId/rewrites/$id")
            .header("X-Api-Key", apiKey)
            .send()
            .check(s"DELETE $name:$ip")

    def skip(name: String, content: String) =
        printHeaderOnce
        println(s"Skipping $name (ip: $content)")

    for Host(name, givenIps) <- hosts do
        val ips = if givenIps.nonEmpty then 
            givenIps.toSet 
        else
            InetAddress.getAllByName(name).map(_.getHostAddress).toSet

        val entries = getProfile.data.filter: entry =>
            entry.get("name").contains(name)
        val ipsInEntries =
            entries.flatMap(_.get("content")).toSet

        val ipsToAdd = ips -- ipsInEntries
        val ipsToRemove = ipsInEntries -- ips
        val ipsToSkip = ips & ipsInEntries

        if verbose then
            for ip <- ipsToSkip do
                skip(name, ip)
        for ip <- ipsToAdd do
            post(name, ip)
        for
            ip <- ipsToRemove
            entry <- entries.find(_.get("content").contains(ip))
        do
            delete(entry("id"), name, ip)

    val toDelete = getProfile.data.filter: entry =>
        !hosts.exists(h => h.name == entry("name"))
    for entry <- toDelete do
        delete(entry("id"), entry("name"), entry("content"))

object Main extends CommandApp(
    name = "nextdns-update",
    header = "Update NextDNS's rewrites based on the current DNS (corporate VPN)",
    main =
        val hostsInput = Opts
            .argument[Path]("hosts-file")
            .map(parseHostsFile)
            .validate("Hosts file must not be empty")(_.nonEmpty)
        val apiKey = Opts
            .option[String]("api-key", help = "NextDNS API key")
            .orElse(Opts.env[String]("NEXTDNS_API_KEY", help = "NextDNS API key"))
        val profileId = Opts
            .option[String]("profile-id", help = "NextDNS profile ID")
            .orElse(Opts.env[String]("NEXTDNS_PROFILE_ID", help = "NextDNS profile ID"))
        val dryRun = Opts
            .flag("dry-run", help = "Dry run").orFalse
        val verbose = Opts
            .flag("verbose", help = "Verbose output (show skipped entries)")
            .orFalse
        (hostsInput, apiKey, profileId, dryRun, verbose)
            .mapN(run)
)
