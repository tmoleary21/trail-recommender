# trail-recommender

Term project for CS555 at CSU built by Tyson O'Leary (@tmoleary21) and Jackson Volesky (@jjvolesky)

## Spark Setup

1. `./spark/configure-spark.sh`
2. Add `export SPARK_HOME=<absolute-path-to-repository>/spark/spark-3.5.1-bin-hadoop3` to `~/.bashrc`

## Spark actions

### Start cluster: `$SPARK_HOME/sbin/start-all.sh`

### Stop cluster: `$SPARK_HOME/sbin/stop-all.sh`


## Notes

* For the `gradle` command, use `module load courses/cs555`
* When submitting the job, first use `module load java/11`