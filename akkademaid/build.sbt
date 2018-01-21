name := """akkademaid"""

version := "1.0"

scalaVersion := "2.11.7"

// Change this to another test framework if you prefer
libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.4" % "test"

libraryDependencies += "com.syncthemall" % "boilerpipe" % "1.2.2"

libraryDependencies += "com.akkademy-db"   %% "akkademy-db"  % "0.0.1-SNAPSHOT"

libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % "2.3.11" % "test"
// Uncomment to use Akka
libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.3.11"

mappings in (Compile, packageBin) ~= { _.filterNot { case (_, name) =>
  Seq("application.conf").contains(name)
}}