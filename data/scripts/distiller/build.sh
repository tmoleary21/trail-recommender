#!/bin/bash
if [[ ! -d ./lib ]]; then
    mkdir lib
fi

pushd lib

echo "Installing pip modules"
./install-pip-modules.sh

echo "Installing GDAL"
./install-gdal.sh

popd
