package org.grouplens.ratingvalue;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import org.grouplens.lenskit.data.pref.PreferenceDomain;
import org.grouplens.lenskit.eval.AlgorithmInstance;
import org.grouplens.lenskit.eval.data.traintest.TTDataSet;
import org.grouplens.lenskit.eval.metrics.AbstractTestUserMetric;
import org.grouplens.lenskit.eval.metrics.TestUserMetricAccumulator;
import org.grouplens.lenskit.eval.traintest.TestUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
public class MutualInformationMetric extends AbstractTestUserMetric {
    private static final Logger logger = LoggerFactory.getLogger(MutualInformationMetric.class);
    private static final String[] COLUMNS = { "MI", "MI.ByUser" };
    private static final String[] USER_COLUMNS = {"MI"};
    private PreferenceDomain domain;

    public MutualInformationMetric(PreferenceDomain domain) {
        this.domain = domain;
    }

    @Override
    public TestUserMetricAccumulator makeAccumulator(AlgorithmInstance algorithm, TTDataSet dataSet) {
        return new Accum(domain);
    }

    @Override
    public String[] getColumnLabels() {
        return COLUMNS;
    }

    @Override
    public String[] getUserColumnLabels() {
        return USER_COLUMNS;
    }
    
    class Accum implements TestUserMetricAccumulator {
        MutualInformationCounter counter;

        private double userMutualInformationSum;
        private int nratings = 0;
        private int nusers = 0;

        private PreferenceDomain domain;

        public Accum(PreferenceDomain domain) {
            this.domain = domain;
            this.counter = new MutualInformationCounter(domain);
        }

        @Override
        public String[] evaluate(TestUser user) {
            int n = 0;
            
            MutualInformationCounter userCounter = new MutualInformationCounter(domain);

            // overall
            for (Long2DoubleMap.Entry e: user.getTestRatings().fast()) {
                if (Double.isNaN(e.getDoubleValue())) continue;
//                double predicted = Utils.binRating(domain, e.getDoubleValue());
                double predicted = e.getDoubleValue();
                double actual = user.getPredictions().get(e.getLongKey());
                counter.count(predicted, actual);
                userCounter.count(predicted, actual);
                n++;
            }
            if (n > 0) {
                nratings += n;
                nusers += 1;
                double uc = userCounter.calculate();
                userMutualInformationSum += uc;
                return new String[]{Double.toString(uc)};
            } else {
                return null;
            }
        }

        @Override
        public String[] finalResults() {
            double v = counter.calculate();
            double uv = userMutualInformationSum / nusers;
            logger.info("MI: overall {}, by-user {}", new Object[] {v, uv});
            return new String[]{
                    Double.toString(v),
                    Double.toString(uv)
            };
        }
    }
}
