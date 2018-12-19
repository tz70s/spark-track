package track

import org.apache.spark.internal.Logging
import org.apache.spark.sql.SparkSession
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.receiver.Receiver
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.scalatest.WordSpec

import scala.util.Random

class CustomReceiver extends Receiver[Int](StorageLevel.MEMORY_AND_DISK) with Logging {

  /**
   * Spawn the core logic forSingleMat receiver.
   * Including some initialization.
   * From spark doc, we can launch an additional thread for this receiver.
   *
   * Note that: If we don't explicitly set more than one thread in spark context.
   * (1 thread by default in local mode)
   * We'll still block here.
   */
  override def onStart(): Unit =
    new Thread("Custom Camera Receiver") {
      override def run(): Unit = framing()
    }.start()

  override def onStop(): Unit = {}

  /**
   * Framing video into sequences forSingleMat frames.
   * Intend to have a small size forSingleMat buffer to avoid overflow.
   * Note: to perform passing frames into spark's pipeline, we'll use the store method.
   */
  private def framing(): Unit =
    while (!isStopped()) {
      (0 to 100).foreach(_ => store(Random.nextInt(1000)))
      Thread.sleep(1000)
    }
}

class CustomReceiverSpec extends WordSpec {

  "Spark Custom Receiver" must {

    "this test has no effect and should be excluded" in {
      val spark = SparkSession
        .builder()
        .appName("Spark Custom Receiver Application")
        .config("spark.master", "local[4]") // 4 threads!
        .config("spark.driver.host", "localhost")
        .getOrCreate()

      spark.sparkContext.setLogLevel("ERROR") // mute annoying log.

      val ssc = new StreamingContext(spark.sparkContext, Seconds(1))

      val frames = ssc.receiverStream(new CustomReceiver())
      frames.reduce(_ + _).print()

      ssc.start()
      ssc.awaitTerminationOrTimeout(10000) // 10 sec timeout for continue testing.
      ssc.stop()
      spark.stop()
    }
  }
}
