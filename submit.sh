#! /bin/bash

rm -rf $SPARK_HOME/work/*

$SPARK_HOME/bin/spark-submit \
    --class trail.App \
    --master spark://nashville:31040 \
    ./build/libs/trail-recommender.jar


# The repositories and packages options below are an alternative (that I found, not that was explicitly given) to building a 
# fat jar (containing all code we wrote and all dependency code). The fat jar was much slower than doing it this way.
# Compilation would take over a minute with fat jar, then running the job would take close to as long
# However, the code started having weird errors with this. If we could figure out why, we could make development much faster
    # --repositories https://repo.maven.apache.org/maven2,https://artifacts.unidata.ucar.edu/repository/unidata-all \
    # --packages org.apache.sedona:sedona-spark-shaded-3.5_2.13:1.5.1,org.datasyslab:geotools-wrapper:1.5.1-28.2 \