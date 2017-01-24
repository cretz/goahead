name := "goahead"

def commonVersion = "0.1.0"

lazy val goahead = inputKey[Unit]("Run goahead")
lazy val buildTestRt = taskKey[Unit]("Build the test RT lib")
lazy val buildRt = taskKey[Unit]("Build the RT lib")

lazy val goaheadInterop = (project in file("interop")).
  settings(
    name := "goahead-interop",
    version := commonVersion,
    crossPaths := false,
    autoScalaLibrary := false
  )

lazy val goaheadCompiler = (project in file("compiler")).
  settings(commonScalaSettings:_*).
  settings(
    name := "goahead-compiler",
    libraryDependencies ++= Seq(
      "com.squareup" % "javapoet" % "1.8.0",
      "org.ow2.asm" % "asm-all" % "6.0_ALPHA",
      "io.circe" %% "circe-core" % "0.6.1",
      "io.circe" %% "circe-generic" % "0.6.1",
      "io.circe" %% "circe-parser" % "0.6.1",
      "com.github.melrief" %% "pureconfig" % "0.5.0",
      "com.google.guava" % "guava" % "19.0",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
      "ch.qos.logback" % "logback-classic" % "1.1.7",
      aetherLib("aether-api"),
      aetherLib("aether-util"),
      aetherLib("aether-impl"),
      aetherLib("aether-connector-basic"),
      aetherLib("aether-transport-http"),
      aetherLib("aether-transport-file"),
      "org.apache.maven" % "maven-aether-provider" % "3.3.9",
      "org.scalactic" %% "scalactic" % "3.0.0" % "test",
      "org.scalatest" %% "scalatest" % "3.0.0" % "test"
    ),
    logBuffered in Test := false,
    testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-oDF"),
    fork in Test := true,

    fork in buildTestRt := true,
    baseDirectory in buildTestRt := (baseDirectory in LocalRootProject).value,
    javaOptions in buildTestRt += "-Xmx4G",
    //javaOptions in buildTestRt ++= Seq(
    //  "-Dcom.sun.management.jmxremote.port=3333",
    //  "-Dcom.sun.management.jmxremote.ssl=false",
    //  "-Dcom.sun.management.jmxremote.authenticate=false"
    //),
    fullRunTask(buildTestRt, Runtime, "goahead.cli.Main", "compile", "-v", "-c", "libs/java/testrt-compile.conf"),

    fork in buildRt := true,
    baseDirectory in buildRt := (baseDirectory in LocalRootProject).value,
    javaOptions in buildRt += "-Xmx4G",
    //javaOptions in buildRt ++= Seq(
    //  "-Dcom.sun.management.jmxremote.port=3333",
    //  "-Dcom.sun.management.jmxremote.ssl=false",
    //  "-Dcom.sun.management.jmxremote.authenticate=false"
    //),
    fullRunTask(buildRt, Runtime, "goahead.cli.Main", "compile", "-v", "-c", "libs/java/rt-compile.conf"),

    fork in goahead := true,
    baseDirectory in goahead := (baseDirectory in LocalRootProject).value,
    javaOptions in goahead += "-Xmx4G",
    fullRunInputTask(goahead, Runtime, "goahead.cli.Main")
  ).dependsOn(goaheadInterop)

def aetherLib(id: String) = "org.eclipse.aether" % id % "1.1.0"

def commonScalaSettings = Seq(
  version := commonVersion,
  scalaVersion := "2.12.1",
  scalacOptions ++= Seq(
    "-deprecation",
    "-encoding", "UTF-8",
    "-feature",
    "-language:existentials",
    "-language:higherKinds",
    "-language:implicitConversions",
    "-unchecked",
    "-Xfatal-warnings",
    "-Xlint",
    "-Yno-adapted-args",
    "-Ywarn-dead-code",
    "-Ywarn-numeric-widen",
    "-Ywarn-value-discard",
    "-Xfuture",
    "-Ywarn-unused-import"
  )
)