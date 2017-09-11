name := """spectra"""
organization := "com.example"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.2"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.0" % Test
libraryDependencies += "org.reactivemongo" %% "play2-reactivemongo" % "0.12.6-play26"
libraryDependencies += "org.reactivemongo" %% "reactivemongo-akkastream" % "0.12.6"



import play.sbt.routes.RoutesKeys

RoutesKeys.routesImport += "play.modules.reactivemongo.PathBindables._"