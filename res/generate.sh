#!/bin/bash

dataset="ml-10m"

python2.6 ./src/main/python/aggregate.py splits/$dataset > res/$dataset.csv

