#!/usr/bin/env -S scala shebang

//> using scala "3.5.1"
//> using dep "com.monovore::decline:2.4.1"
//> using dep "com.lihaoyi::os-lib:0.10.7"
//> using dep "org.scala-lang.modules::scala-xml:2.3.0"
//> using options -java-output-version:17
//> using packaging.graalvmArgs --no-fallback --initialize-at-build-time --enable-url-protocols=http

import cats.syntax.all.given
import com.monovore.decline.*
import scala.xml.*
import javax.xml.parsers.SAXParserFactory
import java.io.File
import scala.util.Try

case class ActiveAgent(pid: String, status: String, key: String)
case class AvailableConfig(key: String, filename: String)

def loadXmlFile(file: File): Elem = 
    val factory = SAXParserFactory.newInstance()
    factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false)
    factory.setFeature("http://xml.org/sax/features/external-general-entities", true)
    factory.setFeature("http://xml.org/sax/features/external-parameter-entities", true)
    factory.setNamespaceAware(false)
    val parser = factory.newSAXParser()
    val source = new InputSource(file.toURI.toString)
    XML.loadXML(source, parser)

def listMyEnabledAgents() =
    os.proc("launchctl", "list")
        .call()
        .out
        .lines()
        .map: line =>
            val Array(pid, status, name) = line.split("\\s+")
            ActiveAgent(pid, status, name)
        .filter(_.key.startsWith("my."))

def listAllMyAgents() =
    os.list(os.home / ".config" / "my-launch-agents")
        .map(_.last)
        .filter(_.startsWith("my."))
        .flatMap: filename =>
            val path = (os.home / ".config" / "my-launch-agents" / filename).toIO
            val xml = loadXmlFile(path)
            val components = xml \ "dict" \ "_"
            components.zipWithIndex.collectFirst:
                case (elem, idx) if elem.text.trim == "Label" => idx
            .flatMap: idx =>
                Try(components(idx + 1).text.trim)
                    .toOption
                    .filter(_.nonEmpty)
            .map: key =>
                AvailableConfig(key, filename)
        .toList

def enableAgent(agent: AvailableConfig, active: Seq[ActiveAgent]) =
    val dest = os.home / "Library" / "LaunchAgents" / agent.filename
    if active.exists(_.key == agent.key) then
        os.proc("launchctl", "unload", "-w", dest).call(check = true)
    os.copy(os.home / ".config" / "my-launch-agents" / agent.filename, dest, replaceExisting = true)
    os.proc("launchctl", "load", "-w", dest).call()

def disableAgent(agent: AvailableConfig, active: Seq[ActiveAgent]) =
    val dest = os.home / "Library" / "LaunchAgents" / agent.filename
    if active.exists(_.key == agent.key) then
        os.proc("launchctl", "unload", "-w", dest).call(check = true)
    os.remove(dest, checkExists = false)

lazy val keysArgument =
    Opts.arguments[String]("key").orElse(
        Opts.options[String]("key", short = "k", help = "The key of the launch agent")
    )

lazy val quietFlag =
    Opts.flag(long = "quiet", short = "q", help = "Less verbose output").orFalse

lazy val list = Command(
    name = "list",
    header = "List all available launch agents",
).apply:
    quietFlag.map: quiet =>
        if !quiet then
            println("-" * 9 + "+" + "-" * 32 + "+" + "-" * 41)
            println("STATUS   | KEY                            | FILENAME")
            println("-" * 9 + "+" + "-" * 32 + "+" + "-" * 41)
        val active = listMyEnabledAgents().map(_.key).toSet
        val all = listAllMyAgents()
        all.foreach: agent =>
            val status = if active.contains(agent.key) then "active" else "disabled"
            if !quiet then
                println(f"$status%-8s | ${agent.key}%-30s | ${agent.filename}%-40s")
            else
                println(f"$status%-8s ${agent.key}%-30s ${agent.filename}%-40s")
        if !quiet then
            println()

lazy val enable = Command(
    name = "enable",
    header = "Enable launch agents",
).apply:
    (keysArgument, quietFlag).mapN: (keys, quiet) =>
        val agents = listAllMyAgents()
        val active = listMyEnabledAgents()
        if !quiet then
            println("\nEnabling launch agents:")
        for key <- keys.toList do
            agents.find(_.key == key) match
                case Some(agent) =>
                    enableAgent(agent, active)
                    if !quiet then
                        println(s" + ${agent.key} (${agent.filename})")
                case None =>
                    System.err.println(s"Agent not found: $key")
                    System.exit(1)
        if !quiet then println()

lazy val disable = Command(
    name = "disable",
    header = "Disable launch agents",
).apply:
    (keysArgument, quietFlag).mapN: (keys, quiet) =>
        val all = listAllMyAgents()
        val enabled = listMyEnabledAgents()
        if !quiet then
            println("\nDisabling launch agents:")
        for key <- keys.toList do
            all.find(_.key == key) match
                case Some(agent) =>
                    disableAgent(agent, enabled)
                    if !quiet then
                        println(s" - ${agent.key} (${agent.filename})")
                case None =>
                    System.err.println(s"\nERROR: Agent not found: $key\n")
                    System.exit(1)
        if !quiet then println()

object Main extends CommandApp(
    name = "my-launch-agents",
    header = "Manage my macOS launch agents",
    main =
        Opts.subcommands(
            list,
            enable,
            disable
        )
)
