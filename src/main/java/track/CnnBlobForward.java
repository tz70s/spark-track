package track;

import org.opencv.core.Mat;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;

import java.util.ArrayList;
import java.util.List;

public class CnnBlobForward {

    private final Net net;

    /**
     * For convenience and performance reason. (avoid scala conversion between java collections)
     * Implement this in Java.
     *
     * <p>example: {{ val layers = CnnBlobForward.of(blob) }}
     *
     * @param blob handled blob for network.
     * @param setup [[NetworkSetup]] case class.
     * @return list of final yolo layer outputs.
     */
    public static List<String> of(Mat blob, NetworkSetup setup) {
        CnnBlobForward forwarder = new CnnBlobForward(setup);
        forwarder.net.setInput(blob);
        return forwarder.getLayerOutputs();
    }

    private List<String> getLayerOutputs() {
        List<String> layerNames = net.getLayerNames();
        List<Integer> nrOfOutLayers = net.getUnconnectedOutLayers().toList();
        // Prepare the output.
        List<String> out = new ArrayList<>(nrOfOutLayers.size());
        for (Integer idx : nrOfOutLayers) {
            String value = layerNames.get(idx - 1);
            out.add(idx, value);
        }
        return out;
    }

    private CnnBlobForward(NetworkSetup setup) {
        this.net = Dnn.readNet(setup.cfg(), setup.weight());
    }
}
