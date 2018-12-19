package track;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.opencv.core.Mat;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;

import java.util.ArrayList;
import java.util.List;

class Forwarder {

    private final Net net;

    private final static Logger log = LogManager.getLogger(Forwarder.class);

    /**
     * Create a forwarder and return the layerOutputs.
     *
     * <p>Note: the net forward should ensure the OpenCV version to 3.4.4^. Or there'll be a JNI
     * error thrown. Ref to: [[https://github.com/opencv/opencv/issues/12324]]
     */
    static List<Mat> ofBlob(Mat blob, NetworkSetup setup) {
        Forwarder forwarder = new Forwarder(setup);
        return forwarder.pipeline(blob);
    }

    private List<Mat> pipeline(Mat blob) {
        net.setInput(blob);
        List<String> layerOutputNames = getLayerOutputNames();
        List<Mat> layerOutputBlobs = new ArrayList<>(layerOutputNames.size());
        log.error("Before: " + layerOutputBlobs);
        net.forward(layerOutputBlobs, layerOutputNames);
        log.error("After: " + layerOutputBlobs);
        return layerOutputBlobs;
    }

    private List<String> getLayerOutputNames() {
        List<String> layerNames = net.getLayerNames();
        List<Integer> nrOfOutLayers = net.getUnconnectedOutLayers().toList();
        List<String> out = new ArrayList<>();
        nrOfOutLayers.forEach((layerNr) -> out.add(layerNames.get(layerNr - 1)));
        return out;
    }

    private Forwarder(NetworkSetup setup) {
        this.net = Dnn.readNet(setup.cfg(), setup.weight());
    }
}
