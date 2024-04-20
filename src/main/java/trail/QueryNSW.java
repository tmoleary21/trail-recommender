package trail;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class QueryNSW {
    private static final String NSW_API_URL = "https://api.weather.gov";

    /**
     * @param point -- a (lat, lon) point
     * @return NWS forecast as JSON (returns String)
     * @throws Exception
     */
    public static String queryForecast(Point point) throws Exception {
        String forecastURLStr = queryGridpoint(point);
        String response = queryAPI(forecastURLStr, true);

        return response;
    }

    /**
     * @param point -- a (lat,lon) point
     * @return a URL in the form of: "https://api.weather.gov/gridpoints/{gridpointCode}/{x},{y}/forecast"
     * @throws Exception
     */
    private static String queryGridpoint(Point point) throws Exception {
        String urlStr = String.format("%s/points/%d%%2C%d", NSW_API_URL, point.getLat(),point.getLon());
        String response = queryAPI(urlStr, false);

        int forecastURLBeginIndex = response.indexOf("\"forecast\": \"") + 13;
        int forecastURLEndIndex = response.indexOf("\"forecastHourly\"") - 10;
        String forecastURLStr = response.substring(forecastURLBeginIndex, forecastURLEndIndex);
        return forecastURLStr;
    }

    private static String queryAPI(String urlStr, boolean addNewLines) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        int status = connection.getResponseCode();
        if (status > 299) {
            System.out.printf("Error in request, status code: %d\n", status);
            return null;
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            if (addNewLines) content.append(inputLine + "\n");
            else content.append(inputLine);
        }
        in.close();

        return content.toString();
    }

    public static void main (String[] args) {
        Point point = new Point(40, -105);
        try {
            System.out.println(queryForecast(point));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
