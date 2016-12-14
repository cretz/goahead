name := "goahead"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "org.ow2.asm" % "asm-all" % "5.1",
  "io.circe" %% "circe-core" % "0.6.1",
  "io.circe" %% "circe-generic" % "0.6.1",
  "io.circe" %% "circe-parser" % "0.6.1",
  "com.github.melrief" %% "pureconfig" % "0.3.3",
  "com.google.guava" % "guava" % "19.0",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
  "ch.qos.logback" % "logback-classic" % "1.1.7",
  "org.scalactic" %% "scalactic" % "3.0.0" % "test",
  "org.scalatest" %% "scalatest" % "3.0.0" % "test",

  // For the jvm.go project's test classes
  "com.google.code.gson" % "gson" % "2.3.1",
  "junit" % "junit" % "4.10",
  "org.eclipse.jetty.aggregate" % "jetty-all" % "9.2.9.v20150224"
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
// Add the jvm.go test classes
unmanagedSourceDirectories in Test += baseDirectory.value / "third-party/jvm.go/testclasses/src/main/java"
// Needed because jvm.go source has some files incompatible w/ default Windows encoding
javacOptions in Test ++= Seq("-encoding", "UTF-8")
// Needed to exclude files that use the internal sun API
excludeFilter in (Test, unmanagedSources) := "*sunmisc*" || "*UnitTests*" || "*CallerClassTest*"

lazy val buildRt = taskKey[Unit]("Build the RT lib")
fullRunTask(buildRt, Runtime, "goahead.cli.Main", "build-rt", "-vv", "-o", "javalib/src/rt")