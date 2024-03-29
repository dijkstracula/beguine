ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.0"

lazy val root = (project in file("."))
  .settings(
    name := "beguine"
  )

resolvers += Resolver.mavenLocal

libraryDependencies += "accord" % "accord" % "1.0-SNAPSHOT" % Test
libraryDependencies += "accord" % "accord" % "1.0-SNAPSHOT" % Test classifier "tests"

libraryDependencies += "com.google.guava" % "guava" % "30.1-jre"
libraryDependencies += "io.github.tudo-aqua" % "z3-turnkey" % "4.8.14"
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.17" % "test"
libraryDependencies += "org.slf4j" % "slf4j-simple" % "2.0.12" % Test
