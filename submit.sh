#! /bin/bash

$SPARK_HOME/bin/spark-submit \
    --class trail.App \
    --master spark://nashville:31040 \
    ./build/libs/trail-recommender.jar