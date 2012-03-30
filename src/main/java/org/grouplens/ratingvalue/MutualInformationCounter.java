package org.grouplens.ratingvalue;

import org.grouplens.lenskit.data.pref.PreferenceDomain;
import org.grouplens.lenskit.norm.PreferenceDomainQuantizer;

/**
 */
public class MutualInformationCounter {
    private final PreferenceDomainQuantizer inQuantizer;
    private final PreferenceDomainQuantizer outQuantizer;
    private final int inCounts[];
    private final int outCounts[];
    private final int pairCounts[][];
    private int nratings = 0;
    
    public MutualInformationCounter(PreferenceDomain inDomain, PreferenceDomain outDomain) {
        inQuantizer = new PreferenceDomainQuantizer(inDomain);
        outQuantizer = new PreferenceDomainQuantizer(outDomain);
        inCounts = new int[inQuantizer.getCount()];
        outCounts = new int[outQuantizer.getCount()];
        pairCounts = new int[inQuantizer.getCount()][outQuantizer.getCount()];
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
        return sum;
    }
}
