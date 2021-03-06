/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2012 Regents of the University of Minnesota and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
import org.grouplens.lenskit.eval.data.crossfold.RandomOrder

import org.grouplens.lenskit.eval.data.CSVDataSourceBuilder
import org.grouplens.lenskit.data.pref.PreferenceDomain
import org.grouplens.lenskit.eval.metrics.predict.CoveragePredictMetric
import org.grouplens.lenskit.eval.metrics.predict.MAEPredictMetric
import org.grouplens.lenskit.eval.metrics.predict.RMSEPredictMetric
import org.grouplens.lenskit.eval.metrics.predict.NDCGPredictMetric
import org.grouplens.ratingvalue.MutualInformationMetric
import org.grouplens.lenskit.RatingPredictor
import org.grouplens.lenskit.knn.item.ItemItemRatingPredictor
import org.grouplens.lenskit.baseline.BaselinePredictor
import org.grouplens.lenskit.baseline.ItemUserMeanPredictor
import org.grouplens.lenskit.params.UserVectorNormalizer
import org.grouplens.lenskit.norm.VectorNormalizer
import org.grouplens.lenskit.norm.BaselineSubtractingNormalizer
import org.grouplens.lenskit.knn.params.SimilarityDamping
import org.grouplens.lenskit.knn.params.NeighborhoodSize
import org.grouplens.lenskit.baseline.BaselineRatingPredictor
import org.grouplens.ratingvalue.NormalizedMAEMetric
import org.grouplens.ratingvalue.NormalizedRMSEMetric
import org.grouplens.ratingvalue.UserFilteringDataSourceWrapper


def buildDir = "pwd".execute().text.trim()
def baselines = [ItemUserMeanPredictor]

def inputDomains = [
        '2' : new PreferenceDomain(1.0, 2.0, 1.0),
        '5' : new PreferenceDomain(1.0, 5.0, 1.0),
        '10' : new PreferenceDomain(0.5, 5.0, 1.0),
        '21' : new PreferenceDomain(-10.0, 10.0, 1.0),
        '101' : new PreferenceDomain(-10.0, 10.0, 0.20),
]

def predictDomains = [
        '2' : new PreferenceDomain(1.0, 5.0, 4.0),
        '5' : new PreferenceDomain(1.0, 5.0, 1.0),
        '10' : new PreferenceDomain(0.5, 5.0, 0.5),
        '20' : new PreferenceDomain(0.25, 5.0, 0.25),
        '50' : new PreferenceDomain(0.10, 5.0, 0.10),
        '100' : new PreferenceDomain(0.05, 5.0, 0.05),
    ]

def datasetConfigs = [
        'ml-10m' : [
                'path' : buildDir + '/ml-10M100K/ratings.dat',
                'delimiter' : '::',
                'domains' : [
                        //'2' : 3.91 / 3.91,
                        //'5' : 3.91 / 4.09,
                        '10' : 3.91 / 4.22,
                ]
        ],
        'jester' : [
                'path' : buildDir + '/jester_ratings.dat',
                'delimiter' : '::',
                'domains' : [
                        //'2' : 15.47 / 15.47,
                        //'5' : 15.47 / 16.39,
                        '10' : 15.47 / 16.55,
                        //'21' : 15.47 / 16.70,
                        //'101' : 15.47 / 17.06
                ]
        ]
]

phony("all") {
    dsKey = 'ml-10m'
    //dsKey = 'jester'
    def dsConfig = datasetConfigs[dsKey]
    for (int i : [0,1,2,3,4,5,10,15,20,30,40,50]) {
        int n = (i == 0) ? 1000 : i;
        for (def entry : dsConfig.domains) {
            def inKey = entry.key
            def sample = entry.value
            def inDomain = inputDomains[inKey]
            depends trainTest(dsKey + '-' + inKey + '-' + n) {
                def pathPrefix = "${buildDir}/splits/${dsKey}/${inKey}-${n}";
                output "${pathPrefix}/eval-results-sampled.csv"
                for (int j = 0; j < 5; j++) {
                    def sampled = new CSVDataSourceBuilder()
                                    .setName("${pathPrefix}/train.${j}.csv")
                                    .setWrapper(new UserFilteringDataSourceWrapper(sample))
                                    .build()
                    dataset {
                        train sampled
                        test "${pathPrefix}/test.${j}.csv"
                    }
                }

                metric CoveragePredictMetric
                metric MAEPredictMetric
                metric RMSEPredictMetric
                metric NDCGPredictMetric
                metric new NormalizedMAEMetric(inDomain)
                metric new NormalizedRMSEMetric(inDomain)

                for (def pd : predictDomains) {
                    metric (new MutualInformationMetric("MI-${pd.key}", inDomain, pd.value, false))
                    metric (new MutualInformationMetric("MI-${pd.key}-corrected", inDomain, pd.value, true))
                }

                for (bl in baselines) {
                    algorithm(bl.simpleName.replaceFirst(/Predictor$/, "")) {
                        setComponent(RatingPredictor, BaselineRatingPredictor)
                        setComponent(BaselinePredictor, bl)
                    }
                }

                algorithm("ItemItem") {
                    setComponent(RatingPredictor, ItemItemRatingPredictor)
                    setComponent(BaselinePredictor, ItemUserMeanPredictor)
                    setComponent(UserVectorNormalizer, VectorNormalizer, BaselineSubtractingNormalizer)
                    set(SimilarityDamping, 100)
                    set(NeighborhoodSize, 30);
                }
            }
        }
    }
}
