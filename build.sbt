name := "goahead"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  //"org.ow2.asm" % "asm-all" % "5.1",
  "org.ow2.asm" % "asm-all" % "6.0_ALPHA",
  "io.circe" %% "circe-core" % "0.6.1",
  "io.circe" %% "circe-generic" % "0.6.1",
  "io.circe" %% "circe-parser" % "0.6.1",
  "com.github.melrief" %% "pureconfig" % "0.3.3",
  "com.google.guava" % "guava" % "19.0",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
  "ch.qos.logback" % "logback-classic" % "1.1.7",
  "org.scalactic" %% "scalactic" % "3.0.0" % "test",
  "org.scalatest" %% "scalatest" % "3.0.0" % "test"
)

// TODO: test with this also to check debug vars and what not
// javacOptions += "-g"

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

logBuffered in Test := false
testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-oDF")
fork in Test := true
//javaOptions in Test ++= Seq(
//  "-Dcom.sun.management.jmxremote.port=3333",
//  "-Dcom.sun.management.jmxremote.ssl=false",
//  "-Dcom.sun.management.jmxremote.authenticate=false"
//)

lazy val buildTestRt = taskKey[Unit]("Build the test RT lib")
fullRunTask(buildTestRt, Test, "goahead.cli.Main", "compile", "-v", "-c", "src/test/resources/test-rt-compile.conf")

lazy val buildRt = taskKey[Unit]("Build the RT lib")
fork in buildRt := true
javaOptions in buildRt += "-Xmx4G"
//javaOptions in buildRt ++= Seq(
//  "-Dcom.sun.management.jmxremote.port=3333",
//  "-Dcom.sun.management.jmxremote.ssl=false",
//  "-Dcom.sun.management.jmxremote.authenticate=false"
//)
fullRunTask(buildRt, Runtime, "goahead.cli.Main", "compile", "-v", "-c", "libs/java/rt-compile.conf")