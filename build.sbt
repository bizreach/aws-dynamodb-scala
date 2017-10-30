name := "aws-dynamodb-scala"

organization := "jp.co.bizreach"

version := "0.0.7-SNAPSHOT"

scalaVersion := "2.12.4"

crossScalaVersions := Seq("2.11.11", scalaVersion.value)

libraryDependencies ++= Seq(
  "org.scala-lang"     %  "scala-reflect" % scalaVersion.value,
  "com.github.seratch" %% "awscala"       % "0.6.1"
)

publishMavenStyle := true

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (version.value.endsWith("SNAPSHOT"))
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

scalacOptions := Seq("-deprecation", "-feature")

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

pomExtra := (
  <url>https://github.com/bizreach/aws-dynamodb-scala</url>
    <licenses>
      <license>
        <name>The Apache Software License, Version 2.0</name>
        <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
      </license>
    </licenses>
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
    </developers>)
