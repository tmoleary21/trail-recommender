import sys
import os
import subprocess
import glob
import json
from forecast import getForecast, Point 
from time import time

def main(input_trails, optimal_temp):
    # input_trails_str = " ".join(trail for trail in input_trails)
    subprocess.run(['../submit.sh', *input_trails])
    # with open('./temp_forecast.json') as temp_forecast:
    #     forecast = json.load(temp_forecast)
    
    recommended_trails = []

    output_file_path = glob.glob('../data/output/*.json')
    with open(output_file_path[0]) as output_file:
        for line in output_file:
            result = json.loads(line)
            result_centroid = json.loads(result["centroid"])

            if "name" in result:
                result["_name"] = result["name"]
                del result["name"]

            if "place_id" in result:
                result["_place_id"] = result["place_id"]
                del result["place_id"]

            # centroid: (lon, lat)            
            result["centroid"] = result_centroid["coordinates"]
            # currently, https://api.weather.gov is down as of 4-24-24. this is the API we use to get the weather
            result["forecast"] = getForecast(Point(result["centroid"][1], result["centroid"][0]))
            # result["forecast"] = forecast
            result["elevationGain"] = result["max_elevat"] - result["min_elevat"]

            recommended_trails.append(result)

    write_results_to_file(recommended_trails, sorted(recommended_trails, key=lambda result: forecast_tuple(result, optimal_temp)))

def forecast_tuple(result, optimal_temp):
    forecast = result["forecast"]    

    temperature = abs(optimal_temp - forecast["temperature"])
    precipitation = forecast["pctPrecipitation"] if forecast["pctPrecipitation"] != None else 0
    windSpeed = int(forecast["windSpeed"][:forecast["windSpeed"].find(" ")])
    
    return (temperature, precipitation, windSpeed)

def write_results_to_file(results, results_by_weather):
    dir_path = "../results"
    if not os.path.exists(dir_path):
        os.mkdir(dir_path)
    with open(f'{dir_path}/results.txt', 'w') as outfile:
        outfile.write("Recommended Trails:\n")
        results_string = json.dumps(results, indent=2, sort_keys=True)
        outfile.write(results_string)       
 
        outfile.write("\n\nRecommended Trails Sorted by Weather:\n")
        results_by_weather_string = json.dumps(results_by_weather, indent=2, sort_keys=True)
        outfile.write(results_by_weather_string)

if __name__ == "__main__":
    optimal_temp = int(sys.argv[1])
    input_trails = []
    for trail in sys.argv[2:]:
        input_trails.append(trail)

    main(input_trails, optimal_temp)
