import requests

NWS_API_URL = "https://api.weather.gov"

class Point:
    def __init__(self, _lat, _lon):
        self.lat = _lat
        self.lon = _lon

def getForecast(point):
    gridpointURL = getGridpoint(point)
    
    response = queryAPI(gridpointURL)
    return formatForecast(response)

def getGridpoint(point):
    pointURL = f"{NWS_API_URL}/points/{point.lat},{point.lon}"
    
    response = queryAPI(pointURL)

    forecastURL = response["properties"]["forecast"]
    return forecastURL

def queryAPI(apiUrl):
    response = requests.get(apiUrl)
    
    if response.status_code < 300:
        return response.json()
    
    print(f"Request unsuccessful, status_code: {response.status_code}")
    return None

def formatForecast(response):
    soonestPeriod = response["properties"]["periods"][0]

    forecast = {
        "shortForecast": soonestPeriod["shortForecast"],
        "detailedForecast": soonestPeriod["detailedForecast"],
        "pctPrecipitation": soonestPeriod["probabilityOfPrecipitation"]["value"],
        "pctRelativeHumidity": soonestPeriod["relativeHumidity"]["value"],
        "temperature": soonestPeriod["temperature"],
        "windSpeed": soonestPeriod["windSpeed"],
        "startTime": soonestPeriod["startTime"],
        "endTime": soonestPeriod["endTime"]
    }

    return forecast

def main():
    point = Point(40,-105)
    getForecast(point)

if __name__ == "__main__":
    main()
