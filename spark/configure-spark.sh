#! /bin/bash

source ~/.bashrc

# https://stackoverflow.com/a/246128
ROOT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

spark_dir=$ROOT_DIR/"spark-3.5.1-bin-hadoop3"
if [ ! -d $spark_dir ]; then

    # Install tar
    wget https://dlcdn.apache.org/spark/spark-3.5.1/spark-3.5.1-bin-hadoop3.tgz -P $ROOT_DIR

    # Untar
    tar -xvzf $ROOT_DIR/spark-3.5.1-bin-hadoop3.tgz -C $ROOT_DIR

    rm $ROOT_DIR/spark-3.5.1-bin-hadoop3.tgz

fi


defaults_file=spark-defaults.conf
# if [ ! -f $spark_dir/conf/$defaults_file]; then

    cp $ROOT_DIR/$defaults_file $spark_dir/conf/$defaults_file

# fi


env_file=spark-env.sh
# if [ ! -f $spark_dir/conf/$env_file]; then

    cp $ROOT_DIR/$env_file $spark_dir/conf/$env_file

# fi


workers=workers
# if [ ! -f $spark_dir/conf/$workers]; then

    cp $ROOT_DIR/$workers $spark_dir/conf/$workers

# fi

# This needs to be in .bashrc file because it needs to be set on all machines in cluster
# export SPARK_HOME=$(readlink -f "$spark_dir")