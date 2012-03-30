package org.grouplens.ratingvalue;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.grouplens.lenskit.data.pref.PreferenceDomain;
import org.grouplens.lenskit.norm.PreferenceDomainQuantizer;

import java.util.HashMap;
import java.util.Map;

/**
 */
public class MutualInformationCounter {
    private final PreferenceDomainQuantizer quantizer;
    private final int actualCounts[];
    private final int predictedCounts[];
    private final int pairCounts[][];
    private int nratings = 0;
    
    public MutualInformationCounter(PreferenceDomain domain) {
        quantizer = new PreferenceDomainQuantizer(domain);
        actualCounts = new int[quantizer.getCount()];
        predictedCounts = new int[quantizer.getCount()];
        pairCounts = new int[quantizer.getCount()][quantizer.getCount()];
    }

    public void count(double predicted, double actual) {
        int pi = quantizer.apply(predicted);
        int ai = quantizer.apply(actual);
        predictedCounts[pi] += 1;
        actualCounts[ai] += 1;
        pairCounts[pi][ai] += 1;
        nratings++;
    }

    public double calculate() {
        double sum = 0.0;
        for (int pi = 0; pi < quantizer.getCount(); pi++) {
            for (int ai = 0; ai < quantizer.getCount(); ai++) {
                double pa = 1.0 * actualCounts[ai] / nratings;
                double pp = 1.0 * predictedCounts[pi] / nratings;
                double pap = 1.0 * pairCounts[pi][ai] / nratings;
                if (pa > 0 && pp > 0 && pap > 0) {
                    sum += pap * Math.log(pap / (pa * pp))/ Math.log(2.0);
                }
            }
        }
        return sum;
    }
}
