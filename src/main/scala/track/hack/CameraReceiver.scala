package track.hack

import org.apache.spark.internal.Logging
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.receiver.Receiver

import scala.concurrent.duration.Duration

object CameraReceiver {
  def apply(interval: Duration): CameraReceiver = new CameraReceiver(interval)
}

/**
 * Custom camera receiver for Spark.
 * The reading is handling via OpenCV library.
 */
class CameraReceiver(interval: Duration) extends Receiver[CapturedMat](StorageLevel.MEMORY_AND_DISK) with Logging {

  /**
   * Spawn the core logic of receiver.
   * Including some initialization.
   * From spark doc, we can launch an additional thread for this receiver.
   */
  override def onStart(): Unit =
    new Thread("Custom Camera Receiver") {
      override def run(): Unit = framing()
    }.start()

  override def onStop(): Unit = {
    // close out some resources?
  }

  /**
   * Framing video into sequences of frames.
   * Intend to have a small size of buffer to avoid overflow.
   * Note: to perform passing frames into spark's pipeline, we'll use the store method.
   */
  private def framing(): Unit = {
    val capture = Capture()

    try {
      while (!isStopped()) {
        val mat = capture.frame()
        store(mat)
        Thread.sleep(interval.toMillis)
      }
    } catch {
      case e: UnexpectedFramingError =>
        log.warn(s"Meet an unexpected framing error, should checkout this. ${e.getMessage}")
        restart("Tolerable exception, restart this.")
      case e: Throwable =>
        log.error(s"Unhandled error in camera receiver, let this crash.")
        throw e
    }
  }
}
