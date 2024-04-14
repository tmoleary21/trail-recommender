import argparse
import geopandas as gpd
import json
import os
import pydeck as pdk
import random

def main(filepath, rows):
    print('Loading GeoJSON')
    
    data = gpd.read_file(filepath, rows=rows)
    data['fill_color'] = data.apply(lambda row: get_random_color(), axis=1)

    # this next line is only needed if the format is not lng/lat (e.g. it is Easting/Northing)
    # data = data.to_crs('EPSG:4326')

    print('Building PyDeck')

    INITIAL_VIEW_STATE = pdk.ViewState(
        latitude = 37.09,
        longitude = -95.71,
        zoom = 4.2,
        max_zoom = 16,
        pitch = 30,
        bearing = 0
    )

    # add pickable for tooltips to show
    geojson = pdk.Layer(
        'GeoJsonLayer',
        data = data,
        opacity=0.5,
        # pickable = True,
        get_fill_color = 'fill_color'
    )

    r = pdk.Deck(
        layers = [geojson],
        initial_view_state = INITIAL_VIEW_STATE,
        map_style = pdk.map_styles.ROAD,
        # tooltip = {
        #     "text": "Property: {properties.property}"
        # }
    )

    out = f'{filepath[0:-5]}-map.html'
    
    r.to_html(
        filename = out,
        open_browser = True
    )

    print(f'{out} written')

def get_random_color():
    return [random.randint(0, 255), random.randint(0, 255), random.randint(0, 255), 255]

if __name__ == '__main__':
    parser = argparse.ArgumentParser(
        description='Visualize the target GeoJSON file using PyDeck'
    )
    parser.add_argument(
        'filepath', help='The location of a GeoJSON format file'
    )
    parser.add_argument(
        '--rows', help='Number of rows of the GeoJSON file to visualize', default=None
    )
    args = parser.parse_args()

    main(os.path.abspath(args.filepath), int(args.rows) if args.rows else args.rows)
