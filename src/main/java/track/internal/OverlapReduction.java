package track.internal;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.opencv.core.Core;
import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.List;

class OverlapReduction {

    private static final Logger log = LogManager.getLogger(OverlapReduction.class);

    /** Run [[MinMaxLoc]] for determined confidence. */
    static ReductionForm from(List<Mat> layerOutputs) {
        List<Integer> classIds = new ArrayList<>();
        List<Double> confidences = new ArrayList<>();

        for (Mat outMat : layerOutputs) {

            // Get the data from outMat.
            final float[] data = new float[(int) outMat.total()];
            outMat.get(0, 0, data);

            for (int rowNr = 0; rowNr < outMat.rows(); rowNr++) {
                // Each row of data has 4 values for location, followed by N confidence values which
                // correspond to the labels
                Mat scores = outMat.row(rowNr).colRange(5, outMat.cols());
                Core.MinMaxLocResult result = Core.minMaxLoc(scores);
                if (result.maxVal > 0) {
                    classIds.add((int) result.maxLoc.x);
                    confidences.add(result.maxVal);
                }
            }
        }

        log.error("Got reduction form, classId: " + classIds + ", confidences: " + confidences);

        return new ReductionForm(classIds, confidences);
    }

    static class ReductionForm {
        final List<Integer> classIds;
        final List<Double> confidences;

        ReductionForm(List<Integer> classIds, List<Double> confidences) {
            this.classIds = classIds;
            this.confidences = confidences;
        }

        @Override
        public String toString() {
            return "ClassIds: " + classIds + ", Confidences: " + confidences;
        }
    }

    private OverlapReduction() {}
}
