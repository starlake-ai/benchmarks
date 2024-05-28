ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.12.19"

lazy val root = (project in file("."))
  .settings(
    name := "bench-spark"
  )

libraryDependencies += "org.apache.spark" %% "spark-core" % "3.5.1" % "provided"
libraryDependencies += "org.apache.spark" %% "spark-sql" % "3.5.1" % "provided"
libraryDependencies += "org.apache.spark" %% "spark-hive" % "3.5.1" % "provided"

Compile / scalaSource := baseDirectory.value
