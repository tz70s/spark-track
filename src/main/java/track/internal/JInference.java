package track.internal;

import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;
import track.InfVal;
import track.NetworkSetup;
import track.Setup;

import java.util.ArrayList;
import java.util.List;

public class JInference {

    private static final float Scale = 1f / 255f;
    private static final int BlobWidth = 416;
    private static final int BlobHeight = 416;

    private final Net net;

    /**
     * For convenience and performance reason. (avoid scala conversion between java collections)
     * Implement this in Java.
     *
     * <p>example: {{ val layers = JInference.runForSingleMat(blob) }}
     *
     * <p>This is actually a factory method for creating internal forwarder and overlap reduction
     * phases.
     *
     * @param image captured image for inference.
     * @param setup [[NetworkSetup]] case class.
     * @return list runForSingleMat final yolo layer outputs, in [[Mat]].
     */
    public static InfVal runForSingleMat(Mat image, Setup setup) {
        List<Mat> layerOutputs = JInference.runSingle(image, setup.getNetSetup());
        OverlapInfVal infVal = new OverlapInfVal(image, layerOutputs, setup.getClasses());
        return infVal.reduceOverlapBoxes();
    }

    private static List<Mat> runSingle(Mat image, NetworkSetup setup) {
        Mat blob = imageIntoBlob(image);
        JInference inference = new JInference(setup);
        return inference.forward(blob);
    }

    /** Convert image mat into blob. */
    private static Mat imageIntoBlob(Mat image) {
        Size size = new Size(BlobWidth, BlobHeight);
        Scalar scalar = new Scalar(0, 0, 0);
        return Dnn.blobFromImage(image, Scale, size, scalar, true, false);
    }

    /**
     * Create a inference and return the layerOutputs.
     *
     * <p>Note: the net forward should ensure the OpenCV version to 3.4.4^. Or there'll be a JNI
     * error thrown. Ref to: [[https://github.com/opencv/opencv/issues/12324]]
     */
    private List<Mat> forward(Mat blob) {
        net.setInput(blob);
        List<String> layerOutputNames = getLayerOutputNames();
        List<Mat> layerOutputBlobs = new ArrayList<>(layerOutputNames.size());
        net.forward(layerOutputBlobs, layerOutputNames);
        return layerOutputBlobs;
    }

    private List<String> getLayerOutputNames() {
        List<String> layerNames = net.getLayerNames();
        List<Integer> nrOfOutLayers = net.getUnconnectedOutLayers().toList();
        List<String> out = new ArrayList<>();
        nrOfOutLayers.forEach((layerNr) -> out.add(layerNames.get(layerNr - 1)));
        return out;
    }

    private JInference(NetworkSetup setup) {
        this.net = Dnn.readNet(setup.getConfig(), setup.getWeight());
    }
}
