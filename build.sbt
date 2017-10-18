import Dependencies._

lazy val twitterHbcStream = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.github.nilsga",
      scalaVersion := "2.12.3",
      version := "0.1.0-SNAPSHOT"
    )),
    name := "Twitter HBC Akka Stream Source",
    libraryDependencies ++= Seq(
      scalaTest % Test,
      akkaStream,
      twitterHbc
    )
  )
