import sbt._

object Dependencies {
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.3"
  lazy val twitterHbc = "com.twitter" % "hbc-twitter4j" % "2.2.0"
  lazy val akkaStream = "com.typesafe.akka" %% "akka-stream" % "2.5.6"
}
