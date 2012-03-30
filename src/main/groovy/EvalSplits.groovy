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


def buildDir = "pwd".execute().text.trim()

def fakeDomains = [
        'binary' : [
                domain : new PreferenceDomain(1.0, 2.0, 1.0),
                thresholds : [4.0] as double[]
        ],
        '5star' : [
                domain : new PreferenceDomain(1.0, 5.0, 1.0),
                thresholds : [2.0,3.0,4.0,5.0] as double[]
        ],
        '5halfstar' : [
                domain : new PreferenceDomain(1.0, 5.0, 0.5),
                thresholds : [1.5,2.0,2.5,3.0,3.5,4.0,4.5,5.0] as double[]
        ]
]

def predictDomains = [
        'binary' : [
                domain : new PreferenceDomain(1.0, 2.0, 1.0),
                thresholds : [4.0] as double[]
        ],
        '5star' : [
                domain : new PreferenceDomain(1.0, 5.0, 1.0),
                thresholds : [2.0,3.0,4.0,5.0] as double[]
        ],
        '5halfstar' : [
                domain : new PreferenceDomain(1.0, 5.0, 0.5),
                thresholds : [1.5,2.0,2.5,3.0,3.5,4.0,4.5,5.0] as double[]
        ]
    ]

def datasetConfigs = [
        'ml-100k' : [
                'path' : buildDir + '/ml-100k/u.data',
                'delimiter' : '\t',
                'domain' : new PreferenceDomain(1.0, 5.0, 1.0)
        ],
        'ml-1m' : [
                'path' : buildDir + '/ml-1m/u.data',
                'delimiter' : '::',
                'domain' : new PreferenceDomain(1.0, 5.0, 1.0)
        ],
        'ml-10m' : [
                'path' : buildDir + '/ml-10M100K/ratings.dat',
                'delimiter' : '::',
                'domain' : new PreferenceDomain(1.0, 5.0, 0.5)
        ],
]

phony("all") {
    dsKey = 'ml-100k'
    def dsConfig = datasetConfigs[dsKey]
    for (int i = 0; i <= 20; i++) {
        int n = (i == 0) ? 1000 : i;
        for (def fd : fakeDomains) {
            depends trainTest(dsKey + '-' + fd.key + '-' + n) {
                def pathPrefix = "${buildDir}/splits/${dsKey}/${fd.key}-${n}";
                output "${pathPrefix}/eval-results.csv"
                for (int j = 0; j < 5; j++) {
                    dataset {
                        train "${pathPrefix}/train.${j}.csv"
                        test "${pathPrefix}/test.${j}.csv"
                    }
                }
                metric CoveragePredictMetric
                metric MAEPredictMetric
                metric RMSEPredictMetric
                metric NDCGPredictMetric
                for (def pd : predictDomains) {
                    metric (new MutualInformationMetric("MI-${pd.key}", fd.value.domain, pd.value.domain))
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