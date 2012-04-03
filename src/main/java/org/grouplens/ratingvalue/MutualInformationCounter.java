package org.grouplens.ratingvalue;

import org.grouplens.lenskit.data.pref.PreferenceDomain;
import org.grouplens.lenskit.norm.PreferenceDomainQuantizer;

/**
 *
 * If corrected is true, then we use the Miller-Maddow correction:
 * Given N observations for variables X and Y (where X can take r
 * values and Y can take c values). the Miller Maddow correction is:
 *
 * MI(X;Y) - (r-1)(c-1)/(2 * N * log 2).
 */
public class MutualInformationCounter {
    private final PreferenceDomainQuantizer inQuantizer;
    private final PreferenceDomainQuantizer outQuantizer;
    private final int inCounts[];
    private final int outCounts[];
    private final int pairCounts[][];
    private int nratings = 0;
    private boolean corrected;

    public MutualInformationCounter(PreferenceDomain inDomain, PreferenceDomain outDomain, boolean corrected) {
        inQuantizer = new PreferenceDomainQuantizer(inDomain);
        outQuantizer = new PreferenceDomainQuantizer(outDomain);
        inCounts = new int[inQuantizer.getCount()];
        outCounts = new int[outQuantizer.getCount()];
        pairCounts = new int[inQuantizer.getCount()][outQuantizer.getCount()];
        this.corrected = corrected;
    }

    public void count(double actual, double predicted) {
        int ai = inQuantizer.apply(actual);
        int pi = outQuantizer.apply(predicted);
        outCounts[pi] += 1;
        inCounts[ai] += 1;
        pairCounts[ai][pi] += 1;
        nratings++;
    }

    public double calculate() {
        double sum = 0.0;
        for (int ai = 0; ai < inQuantizer.getCount(); ai++) {
            for (int pi = 0; pi < outQuantizer.getCount(); pi++) {
                double pa = 1.0 * inCounts[ai] / nratings;
                double pp = 1.0 * outCounts[pi] / nratings;
                double pap = 1.0 * pairCounts[ai][pi] / nratings;
                if (pa > 0 && pp > 0 && pap > 0) {
                    sum += pap * Math.log(pap / (pa * pp))/ Math.log(2.0);
                }
            }
        }
        if (corrected) {
            // subtract Miller Maddow Correction: (r-1)(c-1)/(2 * N * log 2)
            sum -= (inQuantizer.getCount() - 1) * (outQuantizer.getCount() - 1)
                    / (2 * nratings * Math.log(2));
        }
        return sum;
    }
}
