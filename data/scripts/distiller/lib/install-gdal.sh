#!/bin/bash
GDAL_RELEASE=3.7.1
GDAL_NAME=gdal-$GDAL_RELEASE

SRC_DIR=$(pwd)

# Check for Dependencies
echo -e "\n*** Checking Dependencies ***"
if [[ "$OSTYPE" == "darwin"* ]]; then
    brew install gdal —-only-dependencies
    brew install openjdk@11

    export JAVA_HOME="/opt/homebrew/opt/openjdk@11"
    export PATH="$JAVA_HOME/bin:$PATH"
    export EXTRA_ARGS="-DCMAKE_PREFIX_PATH=/opt/homebrew/include"
else
    export EXTRA_ARGS="-DGDAL_USE_ARCHIVE=OFF"
fi

# Check for GDAL
if [[ ! -d $GDAL_NAME ]]
then
    echo -e "\n*** Downloading GDAL ***"
    wget https://github.com/OSGeo/gdal/releases/download/v$GDAL_RELEASE/$GDAL_NAME.tar.gz

    tar -xf $GDAL_NAME.tar.gz
    rm $GDAL_NAME.tar.gz

    echo -e "\n*** Compiling GDAL ***"

    pushd $GDAL_NAME
    mkdir build
    pushd build

    cmake $EXTRA_ARGS -DCMAKE_INSTALL_PREFIX=$SRC_DIR/$GDAL_NAME/build -DCMAKE_BUILD_TYPE=Release ..
    cmake --build .
    cmake --build . --target install

    popd
    popd
fi

# If libarchive is wanted (it seems to be only needed for working with compressed files),
# then add these below lines to the else statement where libarchive is turned off

# ARCHIVE_NAME=libarchive-3.7.1
# if [[ ! -d $ARCHIVE_NAME ]]
# then
#     echo -e "\n*** Downloading libarchive ***"
#     wget https://libarchive.org/downloads/$ARCHIVE_NAME.tar.gz
#     tar -xf $ARCHIVE_NAME.tar.gz
#     rm $ARCHIVE_NAME.tar.gz

#     echo -e "\n*** Compiling libarchive ***"
#     pushd $ARCHIVE_NAME
#     pushd build

#     cmake -DCMAKE_INSTALL_PREFIX=$SRC_DIR/$ARCHIVE_NAME/build ..
#     cmake --build .
#     cmake --build . --target install

#     popd
#     popd
# fi

# export EXTRA_ARGS="-DARCHIVE_LIBRARY=$SRC_DIR/$ARCHIVE_NAME/build/lib/libarchive.so"
# export EXTRA_ARGS="$EXTRA_ARGS -DARCHIVE_INCLUDE_DIR=$SRC_DIR/$ARCHIVE_NAME/build/include"
