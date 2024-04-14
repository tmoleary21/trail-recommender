import argparse
import os
import subprocess

def main(filepath, tolerance):
    out = f'{os.path.splitext(filepath)[0]}-simplified.json'

    simp_command = ['ogr2ogr', '-simplify', tolerance, out, filepath]

    simp_result = subprocess.run(simp_command, capture_output=True, text=True)

    if (simp_result.returncode == 0):
        print(f'{out} written.')
    else:
        raise ValueError(f'{filepath} could not be simplified.')

if __name__ == '__main__':
    parser = argparse.ArgumentParser(
        description='Simplify the target GeoJSON shapes using the target tolerance value'
    )
    parser.add_argument(
        'filepath', help='The location of a GeoJSON format file'
    )
    parser.add_argument(
        'tolerance', help='Tolerance value for simplification'
    )
    args = parser.parse_args()

    main(os.path.abspath(args.filepath), args.tolerance)
