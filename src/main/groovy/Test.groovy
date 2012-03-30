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

println("foo is " + new File("."))