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
import org.grouplens.lenskit.eval.data.crossfold.TimestampOrder
import org.grouplens.ratingvalue.PreferenceDomainMapper


def buildDir = "pwd".execute().text.trim()

def datasetConfigs = [
        'ml-10m' : [
                'path' : buildDir + '/ml-10M100K/ratings.dat',
                'delimiter' : '::',
                'domain' : new PreferenceDomain(0.5, 5.0, 0.5),
                'fakeDomains' : [
                    '2' : [
                        domain : new PreferenceDomain(1.0, 2.0, 1.0),
                        mapping : [0, 0, 0, 0, 0, 0, 1, 1, 1, 1] as int[]
                    ],
                    '5' : [
                        domain : new PreferenceDomain(1.0, 5.0, 1.0),
                        mapping : [0, 0, 0, 1, 1, 2, 2, 3, 3, 4] as int[]
                    ],
                    '10' : [
                        domain : new PreferenceDomain(0.5, 5.0, 0.5),
                        mapping : [0, 1, 2, 3, 4, 5, 6, 7, 8, 9] as int[]
                    ]
                ]
            ],
        'jester' : [
                'path' : buildDir + '/jester_ratings.dat',
                'delimiter' : '::',
                'domain' : new PreferenceDomain(-10.0, 10.0, 0.25),
                'fakeDomains' : [
                    '2' : [
                        domain : new PreferenceDomain(1.0, 2.0, 1.0),
                        mapping : [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1] as int[]
                    ],
                    '5' : [
                        domain : new PreferenceDomain(1.0, 5.0, 1.0),
                        mapping : [0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4] as int[]
                    ],
                    '10' : [
                        domain : new PreferenceDomain(0.5, 5.0, 0.5),
                        mapping : [0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 6, 6, 6, 6, 6, 6, 6, 6, 6, 7, 7, 7, 7, 7, 7, 7, 8, 8, 8, 8, 9, 9, 9, 9, 9, 9, 9, 9] as int[]
                    ],
                    '81' : [
                        domain : new PreferenceDomain(-10.0, 10.0, 0.25),
                        mapping : 0..80 as int[]
                    ]
                ]
            ],
    ]

phony("all") {
    dsKey = 'jester'
    def dsConfig = datasetConfigs[dsKey]
    for (int i : [0,1,2,3,4,5,10,15,20,30,40,50]) {
        int n = (i == 0) ? 1000 : i
        for (def fd : dsConfig.fakeDomains) {
            depends crossfold(dsKey + '-' + fd.key + '-' + n) {
                source csvfile(dsConfig.path) {
                    wrapper { csvDao ->
                        return new RescaledRatingDao.Factory(
                                new PreferenceDomainMapper(dsConfig.domain, fd.value.domain, fd.value.mapping),
                                csvDao
                        );
                    }
                    file dsConfig.path
                    delimiter dsConfig.delimiter
                    domain dsConfig.domain
                }
                holdout 10
                partitions 5
                train "${buildDir}/splits/${dsKey}/${fd.key}-${n}/train.%d.csv"
                test "${buildDir}/splits/${dsKey}/${fd.key}-${n}/test.%d.csv"
                if (n == 1000) {
                    order RandomOrder
                } else {
                    order TimestampOrder
                    partitionAlgorithm new RetainCountPartition(n)
                }
            }
        }
    }
}
