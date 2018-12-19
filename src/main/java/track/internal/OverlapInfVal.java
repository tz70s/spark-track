package track.internal;

import org.opencv.core.*;
import org.opencv.dnn.Dnn;
import org.opencv.imgproc.Imgproc;
import track.InfVal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class OverlapInfVal {

    private static final float ConfidenceThreshold = 0.5f;

    // Perform non maximum suppression (NMS) to eliminate redundant overlapping boxes with lower
    // confidences
    private static final float NmsThreshold = 0.3f;

    private final Mat image;
    private final List<String> classes;

    // Intermediate objects.
    private final List<Integer> classIds = new ArrayList<>();
    private final List<Float> confidences = new ArrayList<>();
    private final List<Rect> boxes = new ArrayList<>();

    // After reduce.
    private final Map<String, Float> classWithConf = new HashMap<>();

    OverlapInfVal(Mat image, List<Mat> layerOutputs, List<String> classes) {
        this.image = image;
        this.classes = classes;
        build(layerOutputs);
    }

    private void build(List<Mat> layerOutputs) {
        for (Mat outMat : layerOutputs) {
            // Get the data via outMat.
            final float[] data = new float[(int) outMat.total()];
            outMat.get(0, 0, data);

            int dataIdx = 0;
            for (int rowNr = 0; rowNr < outMat.rows(); rowNr++) {
                // Each row of data has 4 values for location, followed by N confidence values which
                // correspond to the labels
                Mat scores = outMat.row(rowNr).colRange(5, outMat.cols());
                Core.MinMaxLocResult result = Core.minMaxLoc(scores);
                if (result.maxVal > 0) {
                    classIds.add((int) result.maxLoc.x);
                    confidences.add((float) result.maxVal);
                    boxes.add(buildBox(data, dataIdx, image));
                }
                dataIdx += outMat.cols();
            }
        }
    }

    /** Build box toInfVal floating point matrix data. */
    private Rect buildBox(float[] data, int dataIdx, Mat image) {
        int centerX = (int) (data[dataIdx] * image.cols());
        int centerY = (int) (data[dataIdx + 1] * image.rows());
        int width = (int) (data[dataIdx + 2] * image.cols());
        int height = (int) (data[dataIdx + 3] * image.rows());
        int left = centerX - width / 2;
        int top = centerY - height / 2;
        return new Rect(left, top, width, height);
    }

    /**
     * Join class, confidence and box into labeled image and raw text. At first, this will reduce
     * overlap boxes, then perform the join operations.
     */
    InfVal reduceOverlapBoxes() {
        MatOfInt indexMat = performNms();

        for (int i = 0; i < indexMat.total(); ++i) {
            int idx = (int) indexMat.get(i, 0)[0];
            Rect box = boxes.get(idx);
            String classText = classes.get(classIds.get(idx));
            Float conf = confidences.get(idx);
            classWithConf.put(classText, conf);
            labelWithBox(image, classText, confidences.get(idx), box);
        }

        return InfVal.of(image, classWithConf);
    }

    /**
     * Perform non maximum suppression to eliminate redundant overlapping boxes with lower
     * confidences and sort by confidence.
     */
    private MatOfInt performNms() {
        MatOfInt indexMat = new MatOfInt();
        MatOfRect rectMat = new MatOfRect();
        rectMat.fromList(boxes);
        MatOfFloat confidenceMat = new MatOfFloat();
        confidenceMat.fromList(confidences);
        Dnn.NMSBoxes(rectMat, confidenceMat, ConfidenceThreshold, NmsThreshold, indexMat);
        return indexMat;
    }

    /** This is a mutable operation. In which the image will be labeled with text and around box. */
    private void labelWithBox(Mat image, String classText, Float confidence, Rect box) {
        Imgproc.rectangle(image, box, new Scalar(0, 0, 255));

        String label = String.format("%s : %.2f", classText, confidence);
        int[] baseLine = new int[] {0};
        Size labelSize = Imgproc.getTextSize(label, Imgproc.FONT_HERSHEY_SIMPLEX, 0.5, 1, baseLine);
        int height = box.y > labelSize.height ? box.y : (int) labelSize.height;
        Imgproc.putText(
                image,
                label,
                new Point(box.x, height),
                Imgproc.FONT_HERSHEY_SIMPLEX,
                0.5,
                new Scalar(255, 255, 255));
    }
}
