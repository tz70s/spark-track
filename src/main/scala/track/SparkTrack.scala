package track

import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.{Seconds, StreamingContext}
import track.hack.CameraReceiver

import scala.concurrent.duration._

object SparkTrack {

  val SparkJMXConf = "spark.driver.extraJavaOptions" ->
  Seq("-Dcom.sun.management.jmxremote.port=9292",
      "-Dcom.sun.management.jmxremote.ssl=false",
      "-Dcom.sun.management.jmxremote.authenticate=false").mkString(" ")

  def main(args: Array[String]): Unit = {

    val spark = SparkSession
      .builder()
      .appName("Spark Yolo Track Application")
      .config("spark.master", "local[4]") // 4 threads!
      .config("spark.driver.host", "localhost")
      .config(SparkJMXConf._1, SparkJMXConf._2)
      .getOrCreate()

    spark.sparkContext.setLogLevel("ERROR")

    val setup = Setup.loadWithFallback(spark)

    val ssc = new StreamingContext(spark.sparkContext, Seconds(1))

    val frames = ssc.receiverStream(CameraReceiver(1.second))
    frames
      .map(capturedMat => capturedMat.toMat)
      .map(mat => Serve.forward(mat, setup))
      .map(_.toString)
      .print()

    // Since Spark run in parallel, the exception will not be handled remotely and correctly.
    // Add a shutdown hook to close out.
    sys.addShutdownHook {
      ssc.stop(stopSparkContext = true, stopGracefully = true)
    }

    ssc.start()
    ssc.awaitTermination()
  }
}
