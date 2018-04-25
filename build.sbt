name := """formBuilderSite"""
organization := "com.functionalcapture"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.4"

libraryDependencies += guice
libraryDependencies += ws
libraryDependencies += specs2 % Test

scalacOptions := Seq("-unchecked", "-deprecation", "-feature")

publishTo := Some(
  Resolver.file(
    "formbuildersite",
    new File(Option(System.getenv("RELEASE_DIR")).getOrElse("/tmp"))
  )
)

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.functionalcapture.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.functionalcapture.binders._"
