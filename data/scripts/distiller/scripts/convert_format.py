import argparse
import os
from osgeo import gdal, ogr
import subprocess
import sys

gdal.UseExceptions()

def main(filepath, file_format, file_ext):
    out = f'{os.path.splitext(filepath)[0]}.{file_ext}'
    conv_command = None
    dataset = None

    try:
        dataset = gdal.Open(filepath)
    except Exception:
        # print(error)
        pass

    if dataset:
        print('Trying conversion using GDAL (this may take a while)')
        conv_command = ['gdal_translate', '-of', file_format, filepath, out]
    else:
        try:
            dataset = ogr.Open(filepath)
        except Exception:
            pass

        if dataset:
            print('Trying conversion using OGR (this may take a while)')
            conv_command = ['ogr2ogr', '-f', file_format, out, filepath]

    if not dataset:
        print(f'{os.path.split(filepath)[1]} is an unsupported format', file=sys.stderr)
        sys.exit(1)

    if convert_format(conv_command):
        print(f'{out} written')
    else:
        print(f'{os.path.split(filepath)[1]} could not be converted to {file_format}', file=sys.stderr)
        sys.exit(1)

    dataset = None

def convert_format(conv_command):
    conv_result = subprocess.run(conv_command, capture_output=True, text=True)
    return conv_result.returncode == 0

if __name__ == '__main__':
    parser = argparse.ArgumentParser(
        description='Convert the target file to the specified file type'
    )
    parser.add_argument(
        'filepath', help='The location of a GDAL or OGR format file'
    )
    parser.add_argument(
        'file_format', help='Format to convert the file to, e.g. `GTiff`'
    )
    parser.add_argument(
        'file_ext', help='File extension for the target format, e.g. `tif`'
    )
    args = parser.parse_args()

    main(os.path.abspath(args.filepath), args.file_format, args.file_ext)
