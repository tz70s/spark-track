package track.hack

import org.opencv.core.Mat
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.videoio.VideoCapture
import org.scalatest.{Matchers, WordSpec}

class CaptureSpec extends WordSpec with Matchers {

  Capture.loadCoreLibrary()

  "Video Capturing" must {

    "loads camera correctly, if opened, " in {
      val camera = new VideoCapture(0)
      val frame = new Mat()
      camera.read(frame)
      Imgcodecs.imwrite("data/cature.jpg", frame)
      camera.isOpened shouldBe true
    }
  }
}
