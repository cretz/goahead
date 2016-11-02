name := "goahead"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "org.ow2.asm" % "asm" % "5.1",
  "org.ow2.asm" % "asm-tree" % "5.1",
  "org.ow2.asm" % "asm-util" % "5.1",
  "com.google.guava" % "guava" % "19.0",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
  "ch.qos.logback" % "logback-classic" % "1.1.7",
  "org.scalactic" %% "scalactic" % "3.0.0" % "test",
  "org.scalatest" %% "scalatest" % "3.0.0" % "test"
)

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