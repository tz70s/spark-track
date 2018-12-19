ThisBuild / name := "spark-track"
ThisBuild / version := "0.1"
ThisBuild / scalaVersion := "2.12.8"

// Manually link OpenCV jar file.
val opencv = file("lib/opencv-400.jar")

val JvmOpts = Seq(
  "-Xms512M",
  "-Xmx2G",
  "-Djava.library.path=lib",
  "-Dcom.sun.management.jmxremote.port = 9292",
  "-Dcom.sun.management.jmxremote.ssl = false",
  "-Dcom.sun.management.jmxremote.authenticate = false"
)

val sparkId = "org.apache.spark"
val sparkVersion = "2.4.0"
val sparkStreaming = sparkId %% "spark-streaming" % sparkVersion
val sparkSql = sparkId %% "spark-sql" % sparkVersion

val scalaTestVersion = "3.0.5"
val scalaTest = "org.scalatest" %% "scalatest" % scalaTestVersion % Test

val libs = Seq(sparkStreaming, sparkSql, scalaTest)

lazy val linkLibSettings = Seq(
  Compile / unmanagedJars += opencv,
  Test / unmanagedJars += opencv
)

lazy val jvmForkSettings = Seq(
  run / fork := true,
  run / javaOptions ++= JvmOpts,
  reStart / javaOptions ++= JvmOpts,
  outputStrategy := Some(StdoutOutput)
)

lazy val `spark-track` = (project in file("."))
  .settings(libraryDependencies ++= libs)
  .settings(linkLibSettings)
  .settings(jvmForkSettings)
