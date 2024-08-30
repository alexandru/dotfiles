#!/usr/bin/env -S scala shebang

//> using scala "3.3.1"
//> using toolkit default
//> using dep "com.monovore::decline:2.4.1"
//> using options -java-output-version:17

import cats.syntax.all.given
import com.monovore.decline.*
import sttp.client4.quick.*
import sttp.client4.Response
import upickle.default.*
import java.net.InetAddress
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

val domainsToCleanUp = List(
    "ing.net"
)

val toUpdate = List(
    "ace.ing.net",
    "amyna.ad.ing.net",
    "checkmarx.ad.ing.net",
    "confluence.ing.net",
    "dcr.rtk2.mdpl.ing.net",
    "elk.ro.ing.net",
    "elk.uat.ro.ing.net",
    "employee-authentication.europe.intranet",
    "fs.ing.net",
    "iam.ro.ing.net",
    "id.foundation.mdpl.ing.net",
    "kibana.ro.ing.net",
    "lx-mqst01-pri.st.nix.ro.ing.net",
    "owp2.ro.ing.net",
    "password.ing.net",
    "portalgp.ro.ing.net",
    "proxy-rtk0016-prd.dcr.prod.ichp.ing.net",
    "sdt.ing.net",
    "sm5aaraptr02.ad.ing.net",
    "touchpoint.ing.net",
    "ua.ing.net",
    "useraccess.ing.net",
    // ST (mock)
    "lx-rpest01-pri.st.nix.ro.ing.net",
    "lx-rpest03-pri.st.nix.ro.ing.net",
    // ST (live)
    "lx-rpest02-sec.st.nix.ro.ing.net",
    "lx-rpest04-sec.st.nix.ro.ing.net",
    // UAT
    "lx-rpeuat01-pri.uat.nix.ro.ing.net",
    "lx-rpeuat03-pri.uat.nix.ro.ing.net",
    "lx-rpeuat04-sec.uat.nix.ro.ing.net",
    "lx-rpeuat06-sec.uat.nix.ro.ing.net",
    // Production
    "lx-rpe01-pri.nix.ro.ing.net",
    "lx-rpe03-pri.nix.ro.ing.net",
    "lx-rpe05-pri.nix.ro.ing.net",
    "lx-rpe08-sec.nix.ro.ing.net",
    "lx-rpe10-sec.nix.ro.ing.net",
    "lx-rpe12-sec.nix.ro.ing.net",
)

extension [T](resp: Response[T])
    def check(label: String, debug: Boolean = false): Unit =
        if !resp.code.isSuccess then
            System.err.println(s"ERROR: Failure on $label — HTTP ${resp.code.code}")
            System.exit(1)
        else if debug then
            println(s"DEBUG: Success on $label — HTTP ${resp.code.code}")

def run(apiKey: String, profileId: String, dryRun: Boolean) =
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

    for name <- toUpdate do
        val ips =
            InetAddress.getAllByName(name).map(_.getHostAddress).toSet
        val entries = getProfile.data.filter: entry =>
            entry.get("name").contains(name)
        val ipsInEntries =
            entries.flatMap(_.get("content")).toSet

        val ipsToAdd = ips -- ipsInEntries
        val ipsToRemove = ipsInEntries -- ips

        for ip <- ipsToAdd do
            post(name, ip)

        for
            ip <- ipsToRemove
            entry <- entries.find(_.get("content").contains(ip))
        do
            delete(entry("id"), name, ip)

    for domain <- domainsToCleanUp do
        val toDelete = getProfile.data.filter: entry =>
            entry.getOrElse("name", "").endsWith(domain) &&
            !toUpdate.contains(entry("name"))
        for entry <- toDelete do
            delete(entry("id"), entry("name"), entry("content"))

object Main extends CommandApp(
    name = "nextdns-vpn-update",
    header = "Update NextDNS's rewrites based on the current DNS (corporate VPN)",
    main =
        val apiKey = Opts
            .option[String]("api-key", help = "NextDNS API key")
            .orElse(Opts.env[String]("NEXTDNS_API_KEY", help = "NextDNS API key"))
        val profileId = Opts
            .option[String]("profile-id", help = "NextDNS profile ID")
            .orElse(Opts.env[String]("NEXTDNS_PROFILE_ID", help = "NextDNS profile ID"))
        val dryRun = Opts
            .flag("dry-run", help = "Dry run").orFalse

        (apiKey, profileId, dryRun).mapN(run)
)
