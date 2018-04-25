lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "nl.vorhacker",
      scalaVersion := "2.12.5",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "basetime",

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
      "org.scalatest"        %% "scalatest"        % "3.0.5" % Test,
      "com.typesafe.akka"    %% "akka-http"        % "10.1.1",
      "com.typesafe.akka"    %% "akka-stream"      % "2.5.12",
      "com.typesafe.akka"    %% "akka-persistence" % "2.5.12",
      "org.fusesource.leveldbjni" % "leveldbjni-all" % "1.8",
      "de.heikoseeberger"    %% "akka-http-circe"  % "1.20.1",
      "io.circe"             %% "circe-generic"    % "0.9.1",
      "io.circe"             %% "circe-parser"     % "0.9.1",
      "com.michaelpollmeier" %% "gremlin-scala" % "3.3.2.0",
      "org.apache.tinkerpop" %  "tinkergraph-gremlin" % "3.3.2",
    ),

    parallelExecution in Test := false,
    logBuffered in Test := false,
  )
