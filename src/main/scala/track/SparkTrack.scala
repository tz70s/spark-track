package track

import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.{Seconds, StreamingContext}
import track.hack.CameraReceiver

import scala.concurrent.duration._

object SparkTrack {

  def main(args: Array[String]): Unit = {

    val spark = SparkSession
      .builder()
      .appName("Spark Yolo Track Application")
      .config("spark.master", "local[4]") // 4 threads!
      .config("spark.driver.host", "localhost")
      .getOrCreate()

    // val setup = Setup.loadWithFallback(spark)

    // setup.cls.list.foreach(println)

    val ssc = new StreamingContext(spark.sparkContext, Seconds(1))

    val frames = ssc.receiverStream(CameraReceiver(1.second))
    frames.window(Seconds(10)).count().print()

    ssc.start()
    ssc.awaitTermination()
  }
}
