import Dependencies._

lazy val twitterHbcStream = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.github.nilsga",
      scalaVersion := "2.12.3",
      version := "0.1.0"
    )),
    name := "Twitter HBC Akka Stream Source",
    libraryDependencies ++= Seq(
      scalaTest % Test,
      akkaStream,
      twitterHbc
    ),

    scmInfo := Some(
      ScmInfo(
        url("https://github.com/nilsga/twitter-hbc-akka-stream-source"),
        "git@github.com:nilsga/twitter-hbc-akka-stream-source.git"
      )
    ),

    developers := List(
      Developer(
        id = "nilsga",
        name = "Nils-Helge Garli Hegvik",
        email = "nilsga@gmail.com",
        url = url("http://github.com/nilsga")
      )
    ),

    licenses += ("MIT", url("http://opensource.org/licenses/MIT"))
  )
