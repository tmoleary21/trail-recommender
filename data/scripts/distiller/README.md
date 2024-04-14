
## Project Structure

    % tree .
    .
    ├── build.sh
    ├── lib
    │   ├── install-gdal.sh
    │   ├── install-pip-modules.sh
    │   └── requirements.txt
    ├── scripts
    │   ├── convert_coords.py
    │   ├── convert_format.py
    │   ├── get_dataset_info.py
    │   ├── rapyd_visualizer.py
    │   └── simplify_shapes.py
    └── use-env.sh

## Project Setup

The download scripts are written to setup on a `lattice` machine or a **Mac**. I strongly recommend picking a `lattice` machine & connecting through SSH, because the dependencies are already mostly setup on those machines. Otherwise, you likely will need to download extra dependencies & change the `install-gdal.sh` script. I (Nathan Orwick) can help with that but recommend choosing a lattice machine instead.

First, compile **GDAL**:

    % ./build.sh

**GDAL** is a large project, so this may take a while. Whenever **GDAL** is needed, make sure to run (from the project's directory):

    % source ./use-env.sh

## Get Dataset Info

Use `scripts/get_dataset_info.py` for this. Usage:

    % python scripts/get_dataset_info.py -h
    usage: get_dataset_info.py [-h] [--shapes | --no-shapes] filepath

    Process the metadata for a supported dataset file & optionally build a GeoJSON representation

    positional arguments:
    filepath              The location of a GDAL or OGR supported file

    options:
    -h, --help            show this help message and exit
    --shapes, --no-shapes
                            Create a GeoJSON representation of the file

When this is run with no options, only the metadata for the specified file will be written. If `--shapes` is specified, the shape data will be written as well, to a separate file. Metadata & shape data will both be written in the form of **JSON**. Shape data will be **GeoJSON**, specifically.

Getting metadata & GeoJSON shapes example:

    % python scripts/get_dataset_info.py ej-data.gdb --shapes

## Simplify GeoJSON Shapes

Use `scripts/simplify_shapes.py` for this. Usage:

    % python scripts/simplify_shapes.py -h
    usage: simplify_shapes.py [-h] filepath tolerance

    Simplify the target GeoJSON shapes using the target tolerance value

    positional arguments:
    filepath    The location of a GeoJSON format file
    tolerance   Tolerance value for simplification

    options:
    -h, --help  show this help message and exit

This requires a file & a tolerance value. The unit for the tolerance value is the same as the unit used within the geometry of the shapes & is used for the simplification calculations. This usually takes minutes to run.

I recommend running this & checking a sample of it using `rapyd_visualizer.py`. Shape simplification can be finicky & need experimentation.

GeoJSON shape simplification example:

    % python scripts/simplify_shapes.py geojson-file.json 120

## Convert File Formats

Use `scripts/convert_format.py` for this. Usage:

    % python scripts/convert_format.py -h
    usage: convert_format.py [-h] filepath file_format file_ext

    Convert the target file to the specified file type

    positional arguments:
    filepath     The location of a GDAL or OGR format file
    file_format  Format to convert the file to, e.g. `GTiff`
    file_ext     File extension for the target format, e.g. `tif`

    options:
    -h, --help   show this help message and exit

Please note that this can only convert raster formats to other raster formats (GDAL supported files) or vector formats to other vector formats (OGR supported files).

Raster format conversion example:

    % python scripts/convert_format.py elevation-data.tif JPEG jpg

Vector format conversion example:

    % python scripts/convert_format.py ej-data.gdb GeoJSON json

See the sources section below for more information on supported file types.

## Convert Coordinate Systems

Use `scripts/convert_coords.py` for this. Usage:

    % python scripts/convert_coords.py -h
    usage: convert_coords.py [-h] filepath input_coord_sys target_coord_sys

    Convert the target file to the specified coordinate system

    positional arguments:
    filepath          The location of a GDAL or OGR format file
    input_coord_sys   Current coordinate system of the target file
    target_coord_sys  Coordinate system to convert the file to

    options:
    -h, --help        show this help message and exit

Coordinate system conversion example:

    % python scripts/convert_coords.py elevation-data.tif NAD83 EPSG:4326

See the sources section below for more information on supported coordinate systems.

## Rapyd Visualization

Use `scripts/rapyd_visualizer.py` for this. Usage:

    % python scripts/rapyd_visualizer.py -h
    usage: rapyd_visualizer.py [-h] [--rows ROWS] filepath

    Visualize the target GeoJSON file using PyDeck

    positional arguments:
    filepath     The location of a GeoJSON format file

    options:
    -h, --help   show this help message and exit
    --rows ROWS  Number of rows of the GeoJSON file to visualize

**GeoJSON** files can be large & easily overwhelm **PyDeck**. If it is taking more than a couple minutes or kills the browser when it loads, use the `--rows` option to lessen the load on **PyDeck**. I have found that anywhere from 5,000 to 150,000 shapes can be reasonable depending on the data being used.

Visualization example:

    % python scripts/rapyd_visualizer.py ej-shapes-simplified.json --rows 10000

## Sources & Further Reading

Building GDAL:
- https://gdal.org/development/building_from_source.html

Supported Vector & Raster Formats:
- https://gdal.org/drivers/raster/index.html
- https://gdal.org/drivers/vector/index.html

Supported Coordinate Systems (see the `EPSG` & `NAD` codes in the first link):
- https://github.com/OSGeo/PROJ/blob/master/data/sql/geodetic_datum.sql
- https://gdal.org/tutorials/osr_api_tut.html
- https://epsg.io/
- https://proj.org/en/9.2/faq.html

GDAL Programs Used:
- https://gdal.org/programs/gdalinfo.html
- https://gdal.org/programs/gdal_polygonize.html
- https://gdal.org/programs/gdal_translate.html
- https://gdal.org/programs/gdalwarp.html

OGR Programs Used:
- https://gdal.org/programs/ogrinfo.html
- https://gdal.org/programs/ogr2ogr.html
- https://pydeck.gl/
