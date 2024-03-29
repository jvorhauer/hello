import sbt._
import sbtrelease.ReleasePlugin.autoImport.ReleaseTransformations._

lazy val releaseSettings: Seq[Def.Setting[Seq[ReleaseStep]]] = Seq(
  releaseProcess := Seq[ReleaseStep](
    runClean,
    checkSnapshotDependencies,
    inquireVersions,
    runTest,
    setReleaseVersion,
    commitReleaseVersion,
    tagRelease,
    setNextVersion,
    commitNextVersion,
    pushChanges
  )
)

lazy val root = (project in file("."))
.settings(releaseSettings)
.settings(
  inThisBuild(List(
    organization := "nl.vorhacker",
    scalaVersion := "2.12.5",
    publishArtifact := false,
    skip in publish := true,
    publishTo := Some(Resolver.file("file", new File(Path.userHome.absolutePath + "/.m2/repository"))),
  )),
  name := "basetime",
  skip in publish := true,

  scalacOptions ++= Seq(
    "-deprecation",
    "-encoding",
    "UTF-8",
    "-feature",
    "-language:postfixOps,reflectiveCalls,implicitConversions,higherKinds,existentials",
    "-explaintypes",
    "-unchecked",
    "-Xfatal-warnings",
    "-Xlint:_",
    "-Xfuture",
    "-Yrangepos",
    "-Ywarn-dead-code",
    "-Ywarn-extra-implicit",
    "-Ywarn-inaccessible",
    "-Ywarn-infer-any",
    "-Ywarn-nullary-override",
    "-Ywarn-nullary-unit",
    "-Ywarn-numeric-widen",
    "-Ypartial-unification",
    "-Ywarn-unused:imports",
    "-Ywarn-unused:implicits",
    "-Ywarn-unused:locals",
    "-Ywarn-unused:params",
    "-Ywarn-unused:patvars",
    "-Ywarn-unused:privates"
  ),
  crossPaths := false,

  libraryDependencies ++= Seq(
    "org.scalatest"         %% "scalatest"           % "3.0.5" % Test,
    "com.typesafe.akka"     %% "akka-http"           % "10.1.1",
    "com.typesafe.akka"     %% "akka-stream"         % "2.5.12",
    "com.typesafe.akka"     %% "akka-persistence"    % "2.5.12",
    "com.typesafe.akka"     %% "akka-slf4j"          % "2.5.12",
    "com.twitter"           %% "chill-akka"          % "0.9.2",
    "org.fusesource.leveldbjni" % "leveldbjni-all"   % "1.8",
    "de.heikoseeberger"     %% "akka-http-circe"     % "1.20.1",
    "io.circe"              %% "circe-generic"       % "0.9.3",
    "io.circe"              %% "circe-parser"        % "0.9.3",
    "io.circe"              %% "circe-java8"         % "0.9.3",
    "com.michaelpollmeier"  %% "gremlin-scala"       % "3.3.2.0",
    "org.apache.tinkerpop"  %  "tinkergraph-gremlin" % "3.3.2",
    "ch.qos.logback"        %  "logback-classic"     % "1.2.3",
    "com.typesafe.akka"     %% "akka-testkit"        % "2.5.12" % Test,
  ),

  parallelExecution in Test := false,
  logBuffered in Test := false,
  fork in Test := true,

  buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
  buildInfoPackage := "build",
  buildInfoOptions += BuildInfoOption.BuildTime,

  publishArtifact := false,
  publishTo := Some(Resolver.file("file", new File(Path.userHome.absolutePath + "/.m2/repository"))),
  publishMavenStyle := true,
)
.enablePlugins(BuildInfoPlugin)
