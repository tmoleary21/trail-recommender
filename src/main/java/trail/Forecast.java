package trail;

import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Forecast {
    /**
     * shortForecast: string
     * detailedForecast: string
     * startTime: string
     * endTime: string
     * temperature: number
     * windSpeed: string
     * pctPrecipitation: number
     * pctRelativeHumidity: number
     */
    private final JSONObject forecast;

    public Forecast(JSONObject forecast) {
        this.forecast = forecast;
    }

    public String getShortForecast() {
        return forecast.getString("shortForecast");
    }

    public String getDetailedForecast() {
        return forecast.getString("detailedForecast");
    }

    public String getStartTime() {
        return forecast.getString("startTime");
    }

    public String getEndTime() {
        return forecast.getString("endTime");
    }

    public int getTemperature() {
        return forecast.getInt("temperature");
    }

    public int getWindSpeed() {
        /* "<speed> mph" */
        String windSpeed = forecast.getString("windSpeed");
        return Integer.parseInt(windSpeed.substring(0, windSpeed.indexOf(" ")));
    }

    public int getPrecipitation() {
        Object pctPrecipitation = forecast.get("pctPrecipitation");
        return pctPrecipitation.equals(null) ? 0 : forecast.getInt("pctPrecipitation");
    }

    public int getHumidity() {
        return forecast.getInt("pctRelativeHumidity");
    }

    @Override
    public String toString() {
        return String.format(
                "shortForecast: %s\n" +
                "detailedForecast: %s\n" +
                "timeRange: %s - %s\n" +
                "temperature: %d\n" +
                "windSpeed: %d mph\n" +
                "precipitation: %d%%\n" +
                "relativeHumidity: %d%%\n",
                getShortForecast(),
                getDetailedForecast(),
                getStartTime(),
                getEndTime(),
                getTemperature(),
                getWindSpeed(),
                getPrecipitation(),
                getHumidity()
        );
    }
}
