#! /bin/bash

# The repositories and packages options in the following command are an alternative to building a 
# fat jar (containing all code we wrote and all dependency code). The fat jar was much slower than doing it this way.
# Compilation would take over a minute, then running the job would take close to as long

$SPARK_HOME/bin/spark-submit \
    --class trail.App \
    --master spark://nashville:31040 \
    --repositories https://repo.maven.apache.org/maven2,https://artifacts.unidata.ucar.edu/repository/unidata-all \
    --packages org.apache.sedona:sedona-spark-shaded-3.5_2.12:1.5.1,org.datasyslab:geotools-wrapper:1.5.1-28.2 \
    ./build/libs/trail-recommender.jar