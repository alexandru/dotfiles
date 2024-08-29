#!/usr/bin/env -S scala shebang -q

//> using scala "2.13.14"
//> using dep "com.github.scopt::scopt::4.1.0"

import scopt.{OParser, Read}
import java.time.format.DateTimeFormatter
import java.time._
import java.util.concurrent.TimeUnit
import scala.util.Try

case class Args(
  since: LocalDateTime,
  until: Option[LocalDateTime],
  zoneId: ZoneId,
)

val parsedArgs = {
  val builder = OParser.builder[Args]
  import builder._

  implicit val readsTime: Read[LocalDateTime] =
    implicitly[Read[String]].map { dt =>
      Try(LocalDateTime.parse(dt, DateTimeFormatter.ISO_LOCAL_DATE_TIME))
        .orElse {
          Try(LocalDate.parse(dt, DateTimeFormatter.ISO_LOCAL_DATE))
            .map(_.atTime(LocalTime.of(0, 0, 0, 0)))
        }
        .getOrElse(
          throw new IllegalArgumentException(
            s"Not a valid timestamp, correct format is `yyyy-mm-dd` OR `yyyy-mm-ddTHH:MM:SS`."
          ))
    }

  implicit val readsZoneId: Read[ZoneId] =
    implicitly[Read[String]].map { id =>
      Try(ZoneId.of(id))
        .getOrElse(throw new IllegalArgumentException(s"'$id' is not a valid timezone id"))
    }

  val parser = OParser.sequence(
    programName("time-since.sc"),
    head("time-since", "1.x"),
    arg[LocalDateTime]("<timestamp>")
      .text("Format: `yyyy-mm-dd` or `yyyy-mm-ddTHH:MM:SS`.")
      .action((ts, args) => args.copy(since = ts)),
    opt[LocalDateTime]('u', "until")
      .text("Format: `yyyy-mm-dd` or `yyyy-mm-ddTHH:MM:SS`. Defaults to NOW.")
      .action { (ts, args) => args.copy(until = Some(ts)) },
    opt[ZoneId]('z', "zone-id")
      .text("Example: Europe/Bucharest")
      .action { (id, args) => args.copy(zoneId = id) },
  )
  OParser
    .parse(parser, args, Args(null, None, ZoneId.systemDefault()))
    .getOrElse {
      System.exit(1)
      throw new RuntimeException()
    }
}

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
