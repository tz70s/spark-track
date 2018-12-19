package track

import org.apache.log4j.LogManager
import org.opencv.core._
import track.internal.JInference

object Inference {

  // Upper confidence.
  val ConfidenceThreshold = 0.5f

  // Perform non maximum suppression (NMS) to eliminate redundant overlapping boxes with lower confidences
  val NmsThreshold = 0.3f

  private val log = LogManager.getLogger("Serve")

  /**
   * Forward mat to network and producing output.
   * Similar reference runForSingleMat api usage can see here: [[https://github.com/opencv/opencv/blob/master/modules/dnn/misc/java/test/DnnTensorFlowTest.java]]
   *
   * The detail implementation runForSingleMat calling network refer to [[JInference]].
   *
   * @param mat The input OpenCV matrix.
   */
  def run(mat: Mat, setup: Setup): String = {

    val reductions = JInference.runForSingleMat(mat, setup.net)
    s"$reductions"
  }
}
