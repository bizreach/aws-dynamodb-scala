name := "aws-dynamodb-scala"
organization := "jp.co.bizreach"
scalaVersion := "2.12.6"
crossScalaVersions := Seq("2.11.12", scalaVersion.value)

libraryDependencies ++= Seq(
  "org.scala-lang"     %  "scala-reflect" % scalaVersion.value,
  "com.github.seratch" %% "awscala"       % "0.7.1"
)

scalacOptions := Seq("-deprecation", "-feature")

