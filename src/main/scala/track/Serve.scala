package track

import org.apache.log4j.LogManager
import org.opencv.core._
import org.opencv.dnn.Dnn

import scala.collection.JavaConverters._

case class Prediction(classIds: List[Int], confidences: List[Float]) {
  override def toString: String =
    s"Class Ids: [${classIds.mkString(", ")}], Confidences: [${confidences.mkString(", ")}]"
}

object Serve {

  // Upper confidence.
  val ConfidenceThreshold = 0.5f

  // Perform non maximum suppression (NMS) to eliminate redundant overlapping boxes with lower confidences
  val NmsThreshold = 0.3f

  private val log = LogManager.getLogger("Serve")

  /**
   * Forward mat to network and producing output.
   * Similar reference forSingleMat api usage can see here: [[https://github.com/opencv/opencv/blob/master/modules/dnn/misc/java/test/DnnTensorFlowTest.java]]
   *
   * The detail implementation forSingleMat calling network refer to [[Inference]].
   *
   * @param mat The input OpenCV matrix.
   */
  def forward(mat: Mat, setup: Setup): Prediction = {

    val reductions = Inference.forSingleMat(mat, setup.net)
    log.error(s"Got class ids : ${reductions.classIds}, confidence: ${reductions.confidences}")

    Prediction(reductions.classIds.asScala.map(i => i.toInt).toList,
               reductions.confidences.asScala.map(_.toFloat).toList)
  }

  /**
   * Perform NMS to reduce overlapping boxes.
   * Used for drawing prediction.
   *
   * Not used currently.
   */
  private def reduceOverlap(confidences: Seq[Float]): Unit = {
    val matOfFloat = new MatOfFloat()
    matOfFloat.fromArray(confidences: _*)
    val boxes = new MatOfRect()
    val indices = new MatOfInt()
    Dnn.NMSBoxes(boxes, matOfFloat, ConfidenceThreshold, NmsThreshold, indices)
  }
}
