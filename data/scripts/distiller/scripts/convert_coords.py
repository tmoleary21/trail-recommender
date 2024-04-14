import argparse
import os
from osgeo import gdal, ogr
import subprocess
import sys

gdal.UseExceptions()

def main(filepath, input_coord_sys, target_coord_sys):
    split_filepath = os.path.splitext(filepath)
    out = f'{split_filepath[0]}-reprojected{split_filepath[1]}'
    conv_command = None
    dataset = None

    try:
        dataset = gdal.Open(filepath)
    except Exception:
        # print(error)
        pass

    if dataset:
        print('Trying conversion using GDAL (this may take a while)')
        conv_command = ['gdalwarp', '-s_srs', input_coord_sys, '-t_srs', target_coord_sys, filepath, out]
    else:
        try:
            dataset = ogr.Open(filepath)
        except Exception:
            pass

        if dataset:
            print('Trying conversion using OGR (this may take a while)')
            conv_command = ['ogr2ogr', '-s_srs', input_coord_sys, '-t_srs', target_coord_sys, out, filepath]

    if not dataset:
        print(f'{filepath} is an unsupported format', file=sys.stderr)
        sys.exit(1)

    if convert_coords(conv_command):
        print(f'{out} written')
    else:
        print(f'{filepath} could not be converted from {input_coord_sys} to {target_coord_sys}', file=sys.stderr)
        sys.exit(1)

    dataset = None

def convert_coords(conv_command):
    conv_result = subprocess.run(conv_command, capture_output=True, text=True)
    return conv_result.returncode == 0

if __name__ == '__main__':
    parser = argparse.ArgumentParser(
        description='Convert the target file to the specified coordinate system'
    )
    parser.add_argument(
        'filepath', help='The location of a GDAL or OGR format file'
    )
    parser.add_argument(
        'input_coord_sys', help='Current coordinate system of the target file'
    )
    parser.add_argument(
        'target_coord_sys', help='Coordinate system to convert the file to'
    )
    args = parser.parse_args()

    main(os.path.abspath(args.filepath), args.input_coord_sys, args.target_coord_sys)
