package track

import java.nio.file.{Path, Paths}

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

case class NetworkSetup private (cfg: Path, weight: Path)

object NetworkSetup {
  def apply(cfg: String, weight: String): NetworkSetup =
    new NetworkSetup(Paths.get(cfg), Paths.get(weight))
}

case class YoloClasses private (list: List[String])

object YoloClasses {
  def apply(text: Dataset[String]): YoloClasses = new YoloClasses(text.collect().toList)
}
