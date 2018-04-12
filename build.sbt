name := "aws-dynamodb-scala"
organization := "jp.co.bizreach"
scalaVersion := "2.12.5"
crossScalaVersions := Seq("2.11.12", scalaVersion.value)

libraryDependencies ++= Seq(
  "org.scala-lang"     %  "scala-reflect" % scalaVersion.value,
  "com.github.seratch" %% "awscala"       % "0.6.3"
)

scalacOptions := Seq("-deprecation", "-feature")

