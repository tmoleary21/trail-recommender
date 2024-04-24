import sys
import json

# Converts GeoJSON FeatureCollection to json lines format of the features
def to_json_lines(jsonfile):

    with open(jsonfile, 'r') as f:
        content = json.load(f)['features']
        
    with open(jsonfile+'l', 'a') as f:
        for obj in content:
            json.dump(obj, f)
            f.write('\n')
                
                
if __name__ == '__main__':
    to_json_lines(sys.argv[1])
        
    