name := "akkahttp"

version := "0.1"

scalaVersion := "2.12.3"

libraryDependencies ++= Seq(
// For Akka 2.4.x or 2.5.x
"com.typesafe.akka" %% "akka-http" % "10.0.10",
// Only when running against Akka 2.5 explicitly depend on akka-streams in same version as akka-actor
"com.typesafe.akka" %% "akka-stream" % "2.5.4", // or whatever the latest version is
"com.typesafe.akka" %% "akka-actor"  % "2.5.4", // or whatever the latest version is
"com.typesafe.akka" %% "akka-http-spray-json" % "10.0.10"
)