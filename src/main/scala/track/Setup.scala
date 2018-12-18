package track

import org.apache.spark.sql.{Dataset, SparkSession}

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

case class Setup(cls: YoloClasses, net: NetworkSetup)

case class NetworkSetup(cfg: String, weight: String)

case class YoloClasses private (list: List[String])

object YoloClasses {
  def apply(text: Dataset[String]): YoloClasses = new YoloClasses(text.collect().toList)
}
