name := "quickandderby.dragonfly.ai"
scalaVersion := "2.13.6"
version := "0.022"
organization := "ai.dragonfly.code"

Compile / mainClass := Some("ai.dragonfly.quickandderby.Demo")

libraryDependencies ++= Seq(
  "org.apache.derby" % "derby" % "10.14.2.0",
  "com.zaxxer" % "HikariCP" % "4.0.3" // DB Connection Pooling
)
