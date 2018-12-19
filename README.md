# spark-track

Object tracking via Yolo and Spark, in Scala and Java.

## Execution

In local mode, you can use Spark submit to local cluster, or directly spawn this via `sbt run`.

Note that in cluster mode, there are some known limitation:

* You have to place the Yolo files (class.txt, yolo.cfg, yolo.weights) into data folder; however, this can be changed via serving this in hdfs or any other network storage.
* You have to ensure dependencies (OpenCV), including C++ dylib for JNI can be found in worker node.
* The customized camera receiver read local camera, that means we need to make the driver program camera accessible.

## View

By using java with OpenCV, there's no direct support for GUI display, hence, for simplicity (without something like ScalaFX), use the file watcher in view folder. 

Note that the whatched file should be saved into `data/capture.jpg`, which is hard-coded in `track.hack.Caputre.ImageSavePath`.
Then, `open view/index.html` (in mac) as static.
