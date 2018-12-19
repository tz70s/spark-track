package track;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.opencv.core.Core;
import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.List;

class OverlapReduction {

    static final Logger log = LogManager.getLogger(OverlapReduction.class);

    /** Run [[MinMaxLoc]] for determined confidence. */
    static ReductionForm from(List<Mat> layerOutputs) {
        List<Double> classIds = new ArrayList<>();
        List<Double> confidences = new ArrayList<>();

        for (Mat outMat : layerOutputs) {
            for (int idx = 0; idx < outMat.rows(); idx++) {
                Mat scores = outMat.row(idx).colRange(5, outMat.cols());
                Core.MinMaxLocResult result = Core.minMaxLoc(scores);
                classIds.add(result.maxLoc.x);
                confidences.add(result.maxVal);
            }
        }

        return new ReductionForm(classIds, confidences);
    }

    static class ReductionForm {
        final List<Double> classIds;
        final List<Double> confidences;

        ReductionForm(List<Double> classIds, List<Double> confidences) {
            this.classIds = classIds;
            this.confidences = confidences;
        }
    }

    private OverlapReduction() {}
}
