from argparse import ArgumentParser
import glob
import os
import subprocess

from distiller.scripts.convert_format import main as convert_format

def main(directory_name, simplify):
    complete = 0
    paths = glob.glob(os.path.join(directory_name, "*/*.shp"))
    
    if simplify != 'none':
        directory_name = simplify_shapes(paths, directory_name, simplify)
        paths = glob.glob(os.path.join(directory_name, "*/*.shp"))
    
    
    for path in paths:
        convert_format(path, 'GeoJSON', 'json')
        
        complete += 1
        print(f'Finished {complete}/{len(paths)}')

    return directory_name # So callers know if the output directory changed

def simplify_shapes(paths, directory_name, simplify):
    simplified_dir = simplified_dir_path(directory_name, simplify)
    if not os.path.exists(simplified_dir):
        os.mkdir(simplified_dir)
    
    for path in paths:
        simplified_out_path = os.path.join(simplified_dir, os.path.basename(os.path.dirname(path)))
        if not os.path.exists(simplified_out_path):
            os.mkdir(simplified_out_path)
        subprocess.run(['npx', 'mapshaper', path, '-simplify', f'percentage={simplify}', '-o', 'format=shapefile', simplified_out_path])
    
    print(f'Simplified to directory {simplified_dir}')

    return simplified_dir


def simplified_dir_path(unsimplified_directory, simplify):
    directory = unsimplified_directory.rstrip('/')
    postfix = make_simplified_postfix(simplify)
    return os.path.join(os.path.dirname(directory), f'{os.path.basename(directory)}_simplified_{postfix}/')

def make_simplified_postfix(simplify):
    # Always in percent format
    if '%' in simplify:
        return simplify.replace('%', '').replace('.', '')
    return str(float(simplify)*100).replace('.', '')
    

if __name__ == '__main__':
    parser = ArgumentParser()
    parser.add_argument('directory', help="The directory containing directories of shapefiles")
    parser.add_argument('--simplify', '-s', default='none', help="Percentage of simplification, either 0\%-100\%. If omitted, no simplification")

    parsed = parser.parse_args()

    main(parsed.directory, parsed.simplify)