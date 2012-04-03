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
import org.grouplens.lenskit.RatingPredictor
import org.grouplens.lenskit.eval.data.crossfold.RandomOrder
import org.grouplens.lenskit.eval.metrics.predict.CoveragePredictMetric
import org.grouplens.lenskit.eval.metrics.predict.MAEPredictMetric
import org.grouplens.lenskit.eval.metrics.predict.NDCGPredictMetric
import org.grouplens.lenskit.eval.metrics.predict.RMSEPredictMetric
import org.grouplens.lenskit.knn.CosineSimilarity
import org.grouplens.lenskit.knn.Similarity
import org.grouplens.lenskit.knn.item.ItemItemRatingPredictor
import org.grouplens.lenskit.knn.params.NeighborhoodSize
import org.grouplens.lenskit.knn.params.SimilarityDamping
import org.grouplens.lenskit.knn.params.UserSimilarity
import org.grouplens.lenskit.knn.user.UserUserRatingPredictor
import org.grouplens.lenskit.norm.BaselineSubtractingNormalizer
import org.grouplens.lenskit.norm.MeanVarianceNormalizer
import org.grouplens.lenskit.norm.VectorNormalizer
import org.grouplens.lenskit.params.NormalizerBaseline
import org.grouplens.lenskit.params.PredictNormalizer
import org.grouplens.lenskit.params.UserVectorNormalizer
//import org.grouplens.lenskit.slopeone.SlopeOneRatingPredictor
//import org.grouplens.lenskit.slopeone.WeightedSlopeOneRatingPredictor
//import org.grouplens.lenskit.slopeone.params.DeviationDamping
//import org.grouplens.lenskit.svd.FunkSVDRatingPredictor
//import org.grouplens.lenskit.svd.params.FeatureCount
//import org.grouplens.lenskit.svd.params.IterationCount
import org.grouplens.lenskit.baseline.*
import org.grouplens.ratingvalue.RescaledRatingDao
import org.grouplens.lenskit.data.pref.PreferenceDomain
import org.grouplens.ratingvalue.MutualInformationMetric
import org.grouplens.ratingvalue.RetainCountPartition
import org.grouplens.ratingvalue.NormalizedMAEMetric

def baselines = [ItemUserMeanPredictor]

def buildDir = "pwd".execute().text.trim()

def originalDomain = new PreferenceDomain(1.0, 5.0, 1.0)
def newDomain = new PreferenceDomain(1.0, 5.0, 1.0)
def predictDomain = new PreferenceDomain(1.0, 5.0, 1.0)
//def rescaledWrapper = { csvDao ->
//                        return new RescaledRatingDao.Factory(
//                                originalDomain, newDomain, csvDao,
//                                [4.0] as double[]
//                        );
//                    }

def ml100k = crossfold("ml-100k") {
    source csvfile("${buildDir}/ml-100k/u.data") {
//        wrapper rescaledWrapper
        file "${buildDir}/ml-100k/u.data"
        delimiter "\t"
        domain originalDomain
    }
    order RandomOrder
    holdout 10
    partitions 5
    train "${buildDir}/ml-100k.train.%d.csv"
    test "${buildDir}/ml-100k.test.%d.csv"
}

def ml1m = crossfold("ml-1m") {
    source csvfile("${buildDir}/ml-1m/ratings.dat") {
//        wrapper rescaledWrapper
        file "${buildDir}/ml-1m/ratings.dat"
        delimiter "::"
        domain originalDomain
    }
    order RandomOrder
    holdout 10
    partitions 5
    train "${buildDir}/ml-1m.train.%d.csv"
    test "${buildDir}/ml-1m.test.%d.csv"
    partitionAlgorithm new RetainCountPartition(1)
}


def ml10m = crossfold("ml-10M100K") {
    source csvfile("${buildDir}/ml-10M100K/ratings.dat") {
//        wrapper rescaledWrapper
        file "${buildDir}/ml-10M100K/ratings.dat"
        delimiter "::"
        domain originalDomain
    }
    order RandomOrder
    holdout 10
    partitions 5
    train "${buildDir}/ml-10m.train.%d.csv"
    test "${buildDir}/ml-10m.test.%d.csv"
}

trainTest {
    depends ml100k
    dataset ml100k

    output "${buildDir}/eval-results.csv"
    predictOutput "${buildDir}/eval-preds.csv"

    metric CoveragePredictMetric
    metric MAEPredictMetric
    metric RMSEPredictMetric
    metric NDCGPredictMetric
    metric (new MutualInformationMetric("MI", newDomain, newDomain, false))
    metric (new MutualInformationMetric("MI-corrected", newDomain, newDomain, true))
    metric (new NormalizedMAEMetric(newDomain))

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