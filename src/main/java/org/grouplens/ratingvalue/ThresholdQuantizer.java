package org.grouplens.ratingvalue;

import org.grouplens.lenskit.data.pref.PreferenceDomain;
import org.grouplens.lenskit.norm.PreferenceDomainQuantizer;
import org.grouplens.lenskit.norm.Quantizer;

public class ThresholdQuantizer implements Quantizer {
    private final PreferenceDomainQuantizer outQuantizer;
    private double[] thresholds;

    /**
     * Constructs a new threshold quantizer.
     *
     * @param thresholds The starting value at which a rating is valid
     * For example, for the standard one to five star scale, thresholds
     * would be [1.0, 2.0, 3.0, 4.0, 5.0].
     *
     *  If we were mapping from [0,1.0] to five star, thresholds might
     *  be [0.0, 0.2, 0.4, 0.6, 0.8].
     *
     * @param outDomain The output scale.
     */
    public ThresholdQuantizer(double [] thresholds, PreferenceDomain outDomain) {
        this.thresholds = thresholds;
        this.outQuantizer = new PreferenceDomainQuantizer(outDomain);
    }

    /**
     * Constructs a new threshold quantizer.
     *
     * Thresholds are equally sized. For example,
     * if we were mapping from [0,1.0] to five star, thresholds would
     * be [0.0, 0.2, 0.4, 0.6, 0.8].
     *
     * Note that this will probably not do the right thing if
     * the input scale is not continuous.
     * For example, [1,5] would have thresholds [1.0, 1.8, 2.6, 3.4, 4.2].
     *
     * @param outDomain The output scale.
     */
    public ThresholdQuantizer(PreferenceDomain outDomain) {
        this.outQuantizer = new PreferenceDomainQuantizer(outDomain);
        double min = outDomain.getMinimum();
        double max = outDomain.getMaximum();
        int n = outQuantizer.getCount();
        double d = (max - min) / n;

        thresholds = new double[n];
        for (int i = 1; i < n; i++) {
            thresholds[i] = min + i * d;
        }
    }

    @Override
    public double[] getValues() {
        throw new UnsupportedOperationException();
    }

    @Override
    public double getValue(int i) {
        return outQuantizer.getValue(i);
    }

    @Override
    public int getCount() {
        return outQuantizer.getCount();
    }

    @Override
    public int apply(double val) {
        // if it's less than the minimum, return 0
        if (val < thresholds[0]) {
            return 0;
        }
        for (int i = 0; i < thresholds.length - 1; i++) {
            if (val + 1e-6 < thresholds[i + 1]) {   // make sure 1.999999999999 >= 2.0
                return i;
            }
        }
        return thresholds.length - 1;
    }

    public PreferenceDomainQuantizer getOutQuantizer() {
        return outQuantizer;
    }

    public double[] getThresholds() {
        return thresholds;
    }
}
