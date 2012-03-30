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

import org.grouplens.ratingvalue.RescaledRatingDao
import org.grouplens.lenskit.data.pref.PreferenceDomain
import org.grouplens.ratingvalue.RetainCountPartition


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
                'path' : buildDir + '/ml-1m/ratings.dat',
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
    for (int n = 1; n < 20; n++) {
        for (def fd : fakeDomains) {
            depends crossfold(dsKey + '-' + fd.key + '-' + n) {
                source csvfile(dsConfig.path) {
                    wrapper { csvDao ->
                        return new RescaledRatingDao.Factory(
                                dsConfig.domain,
                                fd.value.domain,
                                csvDao,
                                fd.value.thresholds
                        );
                    }
                    file dsConfig.path
                    delimiter dsConfig.delimiter
                    domain dsConfig.domain
                }
                order RandomOrder
                holdout 10
                partitions 5
                train "${buildDir}/splits/${dsKey}/${fd.key}-${n}/train.%d.csv"
                test "${buildDir}/splits/${dsKey}/${fd.key}-${n}/test.%d.csv"
                partitionAlgorithm new RetainCountPartition(n)
            }
        }
    }
}