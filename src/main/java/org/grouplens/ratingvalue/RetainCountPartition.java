package org.grouplens.ratingvalue;


import org.grouplens.lenskit.eval.data.crossfold.PartitionAlgorithm;

import static java.lang.Math.min;

import java.util.List;

/**
 * Partition a list by retaining a fixed number of elements.
 * @author Shilad Sen <ssen@macalester.edu>
 *
 * @param <E>
 */
public class RetainCountPartition<E> implements PartitionAlgorithm<E> {

    private int count;

    /**
     * Create a count partitioner.
     * @param n The number of items to put in the second (train) partition.
     */
    public RetainCountPartition(int n) {
        count = n;
    }

    @Override
    public int partition(List<E> data) {
        return min(data.size(), count);
    }

}
