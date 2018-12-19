package track

import java.util.{List => JList}

import org.apache.spark.sql.{Dataset, SparkSession}

import scala.collection.JavaConverters._

object DefaultConfig {
  val YoloClassesPath = defaultValue("txt")
  val YoloSetupPath = defaultValue("cfg")
  val YoloWeightsPath = defaultValue("weights")

  private def defaultValue(postfix: String) = s"data/yolov3.$postfix"
}

object Setup {

  import DefaultConfig._

  def loadWithFallback(spark: SparkSession) =
    Setup(YoloClasses(spark.read.textFile(YoloClassesPath)), NetworkSetup(YoloSetupPath, YoloWeightsPath))
}

case class Setup(private val cls: YoloClasses, private val net: NetworkSetup) {

  /** Java API. */
  def getNetSetup: NetworkSetup = net

  /** Java API. */
  def getClasses: JList[String] = cls.list.asJava
}

case class NetworkSetup(private val cfg: String, private val weight: String) {

  /** Java API. */
  def getConfig: String = cfg

  /** Java API. */
  def getWeight: String = weight
}

case class YoloClasses private (list: List[String])

object YoloClasses {
  def apply(text: Dataset[String]): YoloClasses = new YoloClasses(text.collect().toList)
}
