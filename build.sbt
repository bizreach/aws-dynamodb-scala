name := "aws-dynamodb-scala"
organization := "jp.co.bizreach"
scalaVersion := "2.13.1"
crossScalaVersions := Seq("2.11.12", "2.12.8", scalaVersion.value)

libraryDependencies ++= Seq(
  "org.scala-lang"     %  "scala-reflect" % scalaVersion.value,
  "com.github.seratch" %% "awscala"       % "0.8.4",
  "org.scalatest"      %% "scalatest"     % "3.1.1" % Test
)

scalacOptions := Seq("-deprecation", "-feature")

pomExtra := (
  <scm>
    <url>https://github.com/bizreach/aws-dynamodb-scala</url>
    <connection>scm:git:https://github.com/bizreach/aws-dynamodb-scala.git</connection>
  </scm>
  <developers>
    <developer>
      <id>takezoe</id>
      <name>Naoki Takezoe</name>
      <email>naoki.takezoe_at_bizreach.co.jp</email>
      <timezone>+9</timezone>
    </developer>
    <developer>
      <id>shimamoto</id>
      <name>Takako Shimamoto</name>
      <email>takako.shimamoto_at_bizreach.co.jp</email>
      <timezone>+9</timezone>
    </developer>
  </developers>
)
pomIncludeRepository := { _ => false }
publishMavenStyle := true
publishTo := sonatypePublishTo.value
homepage := Some(url(s"https://github.com/bizreach/aws-dynamodb-scala"))
licenses := Seq("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.html"))

sonatypeProfileName := organization.value
releasePublishArtifactsAction := PgpKeys.publishSigned.value
releaseTagName := (version in ThisBuild).value
releaseCrossBuild := true

import ReleaseTransformations._
releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  publishArtifacts,
  releaseStepCommand("sonatypeRelease"),
  setNextVersion,
  commitNextVersion,
  pushChanges
)
