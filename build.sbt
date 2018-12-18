lazy val commonSettings = Seq(
  name := "spark-track",
  version := "0.1",
  scalaVersion := "2.12.8"
)

val sparkId = "org.apache.spark"
val sparkVersion = "2.4.0"
val sparkStreaming = sparkId %% "spark-streaming" % sparkVersion
val sparkSql = sparkId %% "spark-sql" % sparkVersion

val scalaTestVersion = "3.0.5"
val scalaTest = "org.scalatest" %% "scalatest" % scalaTestVersion % Test

val libs = Seq(sparkStreaming, sparkSql, scalaTest)

lazy val `spark-track` = (project in file("."))
  .settings(
    commonSettings,
    libraryDependencies ++= libs,
    Runtime / unmanagedJars += file("lib/opencv-343.jar"),
    Compile / unmanagedJars += file("lib/opencv-343.jar")
  )