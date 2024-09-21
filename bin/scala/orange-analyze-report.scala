#!/usr/bin/env -S scala shebang

//> using scala "3.5.1"
//> using toolkit default
//> using dep "com.monovore::decline:2.4.1"
//> using dep "org.apache.commons:commons-csv:1.11.0"
//> using options -java-output-version:17

import cats.syntax.all.given
import cats.data.Validated
import com.monovore.decline.*
import org.apache.commons.csv.*
import java.io.File
import scala.collection.mutable.ListBuffer
import scala.util.Try

def extract(
    entries: ListBuffer[(String, BigDecimal)], 
    filter: String => Boolean
): BigDecimal =
    val bytes = entries
        .filter: 
            case (tp, _) => filter(tp)
        .map: 
            case (_, amount) => amount
        .sum
    (bytes / (1024 * 1024 * 1024)).setScale(2, BigDecimal.RoundingMode.HALF_UP)

def run(path: File): Unit =
    val in = CSVParser.parse(path, java.nio.charset.StandardCharsets.UTF_8, CSVFormat.DEFAULT)
    var entries = ListBuffer.empty[(String, BigDecimal)]

    in.forEach: record =>
        val tp = record.get(0)
        val amount = record.get(3)
        for amount <- Try(BigDecimal(amount)).toOption do
            entries += ((tp, amount))

    val roaming = extract(entries, _.toLowerCase.contains("roaming"))
    println(f"Roaming:$roaming%6.2f GB")
    val internal = extract(entries, _.toLowerCase.contains("internet inclus"))
    println(f"Local:$internal%8.2f GB")
    val total = roaming + internal
    println(f"Total:$total%8.2f GB")

object Main extends CommandApp(
    name = "orange-analyze-report",
    header = "Analyze CSV file downloaded from My Orange, extrated from:\nhttps://www.orange.ro/myaccount/reshape/invoice-cronos/cronos",
    main =
        Opts
            .argument[String]("path")
            .mapValidated: path =>
                val file = new java.io.File(path)
                if file.exists then
                    Validated.Valid(file)
                else
                    Validated.invalidNel(s"File not found: $path")
            .map(run)
)
