import json


with open('/s/bach/n/under/tmoleary/cs555/term-project/data/raw/cpw_trails/json/Trails_COTREX02072024.json') as f:
    val = json.load(f)
    
for feature in val['features']:
    geometry = feature['geometry']
    if 'null' in json.dumps(geometry):
        print(feature)