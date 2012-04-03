package org.grouplens.ratingvalue;

import org.grouplens.lenskit.data.pref.PreferenceDomain;
import org.grouplens.lenskit.eval.AlgorithmInstance;
import org.grouplens.lenskit.eval.data.traintest.TTDataSet;
import org.grouplens.lenskit.eval.metrics.TestUserMetricAccumulator;
import org.grouplens.lenskit.eval.metrics.predict.RMSEPredictMetric;
import org.grouplens.lenskit.eval.traintest.TestUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NormalizedRMSEMetric extends RMSEPredictMetric {
    private static final Logger logger = LoggerFactory.getLogger(NormalizedRMSEMetric.class);
    private static final String[] COLUMNS = { "NRMSE", "NRMSE.ByUser" };
    private static final String[] USER_COLUMNS = {"NRMSE"};
    private PreferenceDomain domain;

    public NormalizedRMSEMetric(PreferenceDomain domain) {
        this.domain = domain;
    }

    @Override
    public TestUserMetricAccumulator makeAccumulator(AlgorithmInstance algo, TTDataSet ds) {
        return new Accum(super.makeAccumulator(algo, ds));
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
        private TestUserMetricAccumulator delegate;

        Accum(TestUserMetricAccumulator delegate) {
            this.delegate = delegate;
        }

        @Override
        public String[] evaluate(TestUser user) {
            String [] r = delegate.evaluate(user);
            if (r == null) {
                return null;
            }
            assert(r.length == 1);
            double errRate = Double.valueOf(r[0]);
            return new String[] {
                    Double.toString(normalize(errRate))
            };
        }

        @Override
        public String[] finalResults() {
            String [] r = delegate.finalResults();
            if (r == null) {
                return null;
            }
            assert(r.length == 2);
            double errRate = Double.valueOf(r[0]);
            double userErrRate = Double.valueOf(r[1]);;
            logger.info("NRMSE: {}", normalize(errRate));
            return new String[] {
                    Double.toString(normalize(errRate)),
                    Double.toString(normalize(userErrRate))
            };
        }

        private double normalize(double value) {
            return 1.0 * value / (domain.getMaximum() - domain.getMinimum());
        }
    }

}
