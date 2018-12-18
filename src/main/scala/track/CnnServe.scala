package track

import org.opencv.core.{Mat, Scalar, Size}
import org.opencv.dnn.Dnn

object CnnServe {
  val Scale = 0.00392

  // Upper confidence.
  val ConfidenceThreshold = "0.5"

  // Perform non maximum suppression (NMS) to eliminate redundant overlapping boxes with lower confidences
  val NmsThreshold = "0.3"
}

/**
 * Serving Yolo network for object detection.
 */
class CnnServe(setup: Setup) extends Serializable {

  val BlobWidth = 416
  val BlobHeight = 416

  import CnnServe._

  /**
   * Forward mat to network and producing output.
   * Similar reference of api usage can see here: [[https://github.com/opencv/opencv/blob/master/modules/dnn/misc/java/test/DnnTensorFlowTest.java]]
   *
   * @param mat The input OpenCV matrix.
   */
  def forward(mat: Mat) = {
    val blob = Dnn.blobFromImage(mat, Scale, new Size(BlobWidth, BlobHeight), new Scalar(0, 0, 0), true, false)
    val out = CnnBlobForward.of(blob, setup.net) // JList
  }
}
