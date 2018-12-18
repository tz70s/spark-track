package track

import org.apache.spark.sql.SparkSession

object SparkTrack {

  def main(args: Array[String]): Unit = {

    val spark = SparkSession
      .builder()
      .appName("Spark Yolo Track Application")
      .config("spark.master", "local")
      .config("spark.driver.host", "localhost")
      .getOrCreate()

    val setup = Setup.loadWithFallback(spark)

    setup.cls.list.foreach(println)

    spark.stop()
  }
}
