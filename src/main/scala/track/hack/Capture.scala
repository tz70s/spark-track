package track.hack

import java.util.Base64

import org.apache.log4j.LogManager
import org.opencv.core.{Core, Mat}
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.videoio.VideoCapture

import scala.util.control.NonFatal

object Capture {

  @transient private[hack] val log = LogManager.getLogger(classOf[Capture])

  private val ImageSavePath = "data/capture.jpg"

  def apply(): Capture = {
    try {
      loadCoreLibrary()
    } catch {
      case e: UnsatisfiedLinkError =>
        log.error(
          s"Fatal error to load OpenCV native library, a.k.a JNI error." +
          s"Checkout the library linking is correct via JVM options. Message : ${e.getMessage}"
        )
        // Fixme: find a better way to handle this.
        sys.exit(1)
    }
    new Capture()
  }

  def loadCoreLibrary(): Unit =
    System.loadLibrary(Core.NATIVE_LIBRARY_NAME)

  def saveImage(mat: Mat): Unit =
    Imgcodecs.imwrite(ImageSavePath, mat)
}

/**
 * The capture logic for holding video streaming.
 * Note that this is not serializable, hence can't be any field in Spark logic.
 */
private[hack] class Capture() {
  private val camera = new VideoCapture(0)
  private val mat = new Mat()

  def frame(): CapturedMat = {
    try {
      camera.read(mat)
    } catch {
      case NonFatal(t) => throw UnexpectedFramingError("Error occurred while camera reading", t)
    }
    CapturedMat(mat)
  }
}

/**
 * Structure to avoid unserializable OpenCV Mat passing around Spark.
 * This case class should be serializable and pass within spark.
 */
@SerialVersionUID(1L)
case class CapturedMat(rows: Int, cols: Int, tpe: Int, data: String, cameraId: Int = 0) {
  def toMat: Mat = {
    val mat = new Mat(rows, cols, tpe)
    mat.put(0, 0, Base64.getDecoder.decode(data))
    mat
  }

  override def toString: String =
    // Omit data value.
    s"CapturedMat(rows = $rows, cols = $cols, tpe = $tpe, data=omit, cameraId = $cameraId)"
}

object CapturedMat {
  def apply(mat: Mat): CapturedMat = {
    val data = new Array[Byte]((mat.total() * mat.channels()).asInstanceOf[Int])
    mat.get(0, 0, data)
    CapturedMat(mat.rows(), mat.cols(), mat.`type`(), Base64.getEncoder.encodeToString(data))
  }
}

case class UnexpectedFramingError(message: String, cause: Throwable = null) extends Exception(message, cause)
