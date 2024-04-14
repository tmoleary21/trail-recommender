import argparse
import json
import os
import subprocess
import sys

def main(filepath, should_create_shapes):
    metadata = None

    gdal_was_successful = process_file(filepath, 'gdal', should_create_shapes)

    if (gdal_was_successful):
        return

    ogr_was_successful = process_file(filepath, 'ogr', should_create_shapes)

    if (ogr_was_successful):
        return

    raise ValueError(f'{filepath} is an unsupported format.')

def process_file(filepath, driver, should_create_shapes):
    status = process_metadata(filepath, driver)

    if (should_create_shapes):
        process_shapes(filepath, driver)

    return status

def process_metadata(filepath, driver):
    print(f'Trying {driver} for metadata.')
    info_command = None

    if (driver == 'gdal'):
        info_command = ['gdalinfo', '-json', '-stats', filepath]
    else:
        info_command = ['ogrinfo', '-json', filepath] # -features doesn't work for huge datasets

    info_result = subprocess.run(info_command, capture_output=True, text=True)

    if (info_result.returncode == 0):
        metadata = json.loads(info_result.stdout)

        with open(f'{os.path.splitext(filepath)[0]}-metadata.json', 'w') as out:
            json.dump(metadata, out, indent=4)
            out.write('\n')
            print(f'{out.name} written.')

        return True
    else:
        print(f'{driver} for metadata failed.', file=sys.stderr)
        return False

def process_shapes(filepath, driver):
    print(f'Trying {driver} for GeoJSON. This might take a while!')
    out = f'{os.path.splitext(filepath)[0]}-shapedata.json'
    shape_command = None

    if (driver == 'gdal'):
        shape_command = ['gdal_polygonize.py', filepath, '-f', 'GeoJSON', out]
    else:
        shape_command = ['ogr2ogr', '-f', 'GeoJSON', out, filepath]

    shape_result = subprocess.run(shape_command, capture_output=True, text=True)

    if (shape_result.returncode == 0):
        print(f'{out} written.')
    else:
        print(f'{driver} for GeoJSON failed.', file=sys.stderr)

if __name__ == '__main__':
    parser = argparse.ArgumentParser(
        description='Process the metadata for a supported dataset file & optionally build a GeoJSON representation'
    )
    parser.add_argument(
        'filepath', help='The location of a GDAL or OGR supported file'
    )
    parser.add_argument(
        '--shapes', help='Create a GeoJSON representation of the file', default=False, action=argparse.BooleanOptionalAction
    )
    args = parser.parse_args()

    main(os.path.abspath(args.filepath), args.shapes)
