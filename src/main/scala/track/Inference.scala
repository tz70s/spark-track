package track

import java.lang.{Float => JFloat}
import java.util.{Map => JMap}

import org.opencv.core._
import track.internal.JInference

import scala.collection.JavaConverters._

/**
 * Shorthand for Inference Value.
 * Carry with a image (matrix) with labels and boxes, and pure labels and confidences.
 */
case class InfVal(labeledImage: Mat, classWithConf: Map[String, Float]) {
  override def toString: String =
    s"Class with confidence: $classWithConf"
}

object InfVal {

  /**
   * Java API.
   */
  def of(labeledImage: Mat, classWithConf: JMap[String, JFloat]) =
    InfVal(labeledImage, classWithConf.asScala.toMap.mapValues(jFloat => float2Float(jFloat)))
}

object Inference {

  /**
   * Forward mat to network and producing output.
   * Similar reference runForSingleMat api usage can see here: [[https://github.com/opencv/opencv/blob/master/modules/dnn/misc/java/test/DnnTensorFlowTest.java]]
   *
   * The detail implementation runForSingleMat calling network refer to [[JInference]].
   *
   * @param mat The input OpenCV matrix.
   */
  def run(mat: Mat, setup: Setup): InfVal =
    JInference.runForSingleMat(mat, setup)
}
