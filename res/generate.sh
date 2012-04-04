#!/bin/bash

for dataset in jester ml-10m; do
    python2.6 ./src/main/python/aggregate.py splits/$dataset > res/$dataset.csv
done

