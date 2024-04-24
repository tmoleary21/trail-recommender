import sys
import os
import subprocess
import glob
import json
from forecast import getForecast, Point 

def main(input_trails, optimal_temp):
    input_trails_str = " ".join(trail for trail in input_trails)
    #subprocess.run(f'../submit.sh {input_trails_str}', shell=True)
    
    with open('./temp_forecast.json') as temp_forecast:
        forecast = json.load(temp_forecast)
    
    recommended_trails = []

    output_file_path = glob.glob('../data/output/*.json')
    with open(output_file_path[0]) as output_file:
        for line in output_file:
            result = json.loads(line)
            result_centroid = json.loads(result["centroid"])

            # centroid: (lon, lat)            
            result["centroid"] = result_centroid["coordinates"]
            # currently, https://api.weather.gov is down as of 4-24-24. this is the API we use to get the weather
            # result["forecast"] = getForecast(Point(result["centroid"][1], result["centroid][0]))
            result["forecast"] = forecast
            recommended_trails.append(result)

    write_results_to_file(recommended_trails, sorted(recommended_trails, key=lambda result: forecast_tuple(result, optimal_temp)))
 
def forecast_tuple(result, optimal_temp):
    forecast = result["forecast"]    

    temperature = abs(optimal_temp - forecast["temperature"])
    precipitation = forecast["pctPrecipitation"] if forecast["pctPrecipitation"] != None else 0
    windSpeed = int(forecast["windSpeed"][:forecast["windSpeed"].find(" ")])
    
    return (temperature, precipitation, windSpeed)

def write_results_to_file(results, results_by_weather):
    subprocess.run('rm ../results/results.txt', shell=True)
    with open('../results/results.txt', 'w') as outfile:
        outfile.write("Recommended Trails:\n")
        results_string = json.dumps(results, indent=2)
        outfile.write(results_string)       
 
        outfile.write("\n\nRecommended Trails Sorted by Weather:\n")
        results_by_weather_string = json.dumps(results_by_weather, indent=2)
        outfile.write(results_by_weather_string)

if __name__ == "__main__":
    optimal_temp = int(sys.argv[1])
    input_trails = []
    for trail in sys.argv[2:]:
        input_trails.append(trail)

    main(input_trails, optimal_temp)
