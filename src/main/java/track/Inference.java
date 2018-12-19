package track;

import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.dnn.Dnn;

import java.util.List;

import static track.OverlapReduction.ReductionForm;

public class Inference {

    private static final float Scale = 1f / 255f;
    private static final int BlobWidth = 416;
    private static final int BlobHeight = 416;

    /**
     * For convenience and performance reason. (avoid scala conversion between java collections)
     * Implement this in Java.
     *
     * <p>example: {{ val layers = Inference.forSingleMat(blob) }}
     *
     * <p>This is actually a factory method for creating internal forwarder and overlap reduction
     * phases.
     *
     * @param mat handled blob for network.
     * @param setup [[NetworkSetup]] case class.
     * @return list forSingleMat final yolo layer outputs, in [[Mat]].
     */
    public static ReductionForm forSingleMat(Mat mat, NetworkSetup setup) {
        Mat blob = matIntoBlob(mat);
        List<Mat> layerOutputs = Forwarder.ofBlob(blob, setup);
        return OverlapReduction.from(layerOutputs);
    }

    /** Convert image mat into blob. */
    private static Mat matIntoBlob(Mat mat) {
        Size size = new Size(BlobWidth, BlobHeight);
        Scalar scalar = new Scalar(0, 0, 0);
        return Dnn.blobFromImage(mat, Scale, size, scalar, true, false);
    }
}
