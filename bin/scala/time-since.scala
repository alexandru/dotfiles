#!/usr/bin/env -S scala shebang

//> using scala "3.5.1"
//> using dep "com.monovore::decline:2.4.1"
//> using packaging.graalvmArgs --no-fallback --initialize-at-build-time

import cats.syntax.all.given
import cats.data.{Validated, ValidatedNel}
import com.monovore.decline.*
import com.monovore.decline.time.defaultZoneId

import java.time.*
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import scala.util.Try

case class Args(
    since: LocalDateTime,
    until: Option[LocalDateTime],
    zoneId: ZoneId,
)

given Argument[LocalDateTime] with
    override val defaultMetavar = "iso-local-date-time"

    override def read(string: String): ValidatedNel[String, LocalDateTime] =
        Try(
            LocalDateTime.parse(string, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        )
        .orElse(
            Try(LocalDate.parse(string, DateTimeFormatter.ISO_LOCAL_DATE))
                .map(_.atTime(LocalTime.of(0, 0, 0, 0)))
        )
        .map(Validated.valid)
        .getOrElse(Validated.invalidNel(
            "Not a valid timestamp, correct format is `yyyy-mm-dd` OR `yyyy-mm-ddTHH:MM:SS`."
        ))

def run(parsedArgs: Args): Unit =
    val since = parsedArgs.since.atZone(parsedArgs.zoneId)
    val until = parsedArgs.until.fold(ZonedDateTime.now(parsedArgs.zoneId))(_.atZone(parsedArgs.zoneId))
    val sinceTs = since.toInstant.toEpochMilli
    val untilTs = until.toInstant.toEpochMilli

    println()
    println(s"Since:   ${since.format(DateTimeFormatter.RFC_1123_DATE_TIME)}")
    println(s"Until:   ${until.format(DateTimeFormatter.RFC_1123_DATE_TIME)}")

    val totalMs = untilTs - sinceTs
    val days = TimeUnit.MILLISECONDS.toDays(totalMs)
    val rem1 = totalMs - TimeUnit.DAYS.toMillis(days)
    val hours = TimeUnit.MILLISECONDS.toHours(rem1)
    val rem2 = rem1 - TimeUnit.HOURS.toMillis(hours)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(rem2)
    val rem3 = rem2 - TimeUnit.MINUTES.toMillis(minutes)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(rem3)

    println()
    println(s"Elapsed: $days days, $hours hours, $minutes minutes, $seconds seconds")
    println()

    println(f"Years:   ${(untilTs - sinceTs) / (1000.0 * 60 * 60 * 24 * 365)}%11.2f")
    println(f"Months:  ${(untilTs - sinceTs) / (1000.0 * 60 * 60 * 24 * 30.417)}%11.2f")
    println(f"Weeks:   ${(untilTs - sinceTs) / (1000.0 * 60 * 60 * 24 * 7)}%11.2f")
    println(f"Days:    ${(untilTs - sinceTs) / (1000.0 * 60 * 60 * 24)}%11.2f")
    println(f"Hours:   ${(untilTs - sinceTs) / (1000.0 * 60 * 60)}%11.2f")
    println(f"Minutes: ${(untilTs - sinceTs) / (1000.0 * 60)}%11.2f")
    println()

object Main extends CommandApp(
    name = "time-since",
    header = "Calculate the time since a given timestamp.",
    version = "1.x",
    main = {
        val since = Opts.argument[LocalDateTime]("since").orElse(
            Opts.option[LocalDateTime]("since", "Format: `yyyy-mm-dd` or `yyyy-mm-ddTHH:MM:SS`.")
        )
        val until = Opts
            .option[LocalDateTime]("until", "Format: `yyyy-mm-dd` or `yyyy-mm-ddTHH:MM:SS`. Default is NOW.")
            .orNone
        val zoneId = Opts
            .option[ZoneId](
                short = "z",
                long = "zone-id", 
                help = "Example: Europe/Bucharest"
            )
            .withDefault(ZoneId.systemDefault())
        (since, until, zoneId)
            .mapN(Args.apply)
            .map(run)
    }
)
