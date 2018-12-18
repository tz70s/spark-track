lazy val commonSettings = Seq(
  name := "spark-track",
  version := "0.1",
  scalaVersion := "2.12.8"
)

val sparkId = "org.apache.spark"
val sparkVersion = "2.4.0"
val sparkStreaming = sparkId %% "spark-streaming" % sparkVersion
val sparkSql = sparkId %% "spark-sql" % sparkVersion

val sparkLibs = Seq(sparkStreaming, sparkSql)

lazy val `spark-track` = (project in file("."))
  .settings(
    commonSettings,
    libraryDependencies ++= sparkLibs
  )