ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.0"

lazy val root = (project in file("."))
  .settings(
    name := "beguine"
  )

resolvers += Resolver.mavenLocal

libraryDependencies += "com.google.guava" % "guava" % "30.1-jre"
libraryDependencies += "io.github.tudo-aqua" % "z3-turnkey" % "4.8.14"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.15" % "test"
