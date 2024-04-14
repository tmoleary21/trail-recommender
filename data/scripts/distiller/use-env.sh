#!/bin/bash
GDAL=$PWD/lib/gdal-3.7.1/build

export PATH=$GDAL/bin:$PATH
export GDAL_DATA=$GDAL/share/gdal

if [[ "$OSTYPE" == "darwin"* ]]; then
    export DYLD_LIBRARY_PATH=$GDAL:$DYLD_LIBRARY_PATH
else
    export LD_LIBRARY_PATH=$GDAL:$LD_LIBRARY_PATH
fi

source ./lib/venv/bin/activate

export PYTHONPATH=$GDAL/swig/python:$PYTHONPATH
