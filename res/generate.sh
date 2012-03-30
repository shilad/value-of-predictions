#!/bin/bash

dataset="ml-100k"
metrics="RMSE.ByRating RMSE.ByUser MAE MAE.ByUser MI MI.ByUser"

for m in $metrics; do
    python2.6 ./src/main/python/aggregate.py $m splits/$dataset >res/$dataset.$m.csv
done

