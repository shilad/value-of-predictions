#!/bin/bash

#for dataset in jester ml-10m; do
for dataset in jester; do
    python2.6 ./src/main/python/aggregate_sample.py splits/$dataset > res/$dataset.sampled.csv
done

for dataset in jester; do
    python2.6 ./src/main/python/aggregate.py splits/$dataset > res/$dataset.csv
done

