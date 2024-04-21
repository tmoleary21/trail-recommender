package trail;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class QueryFlask {
    private static final String FLASK_SERVER = "http://phoenix:6000/forecast";

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

    private static String queryFlaskServer(String serverUrl, Point point) throws Exception {
        URL flaskURL = new URL(serverUrl);
        HttpURLConnection connection = (HttpURLConnection) flaskURL.openConnection();

        connection.addRequestProperty("Content-Type", "application/" + "json");
        String query = String.format("{\"lat\": %d, \"lon\": %d}", point.getLat(), point.getLon());
        connection.setRequestProperty("Content-Length", Integer.toString(query.length()));
        connection.getOutputStream().write(query.getBytes("UTF-8"));
        connection.setRequestMethod("POST");

        int status = connection.getResponseCode();
        if (status > 299) {
            System.out.printf("Error in trail-flask-server/forecast request, status code: %d\n", status);
            return null;
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();

        return content.toString();
    }

    public static void main (String[] args) {
        Point point = new Point(40, -105);
        try {
            System.out.println(queryFlaskServer(FLASK_SERVER, point));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

//    /**
//     * @param point -- a (lat, lon) point
//     * @return NWS forecast as JSON (returns String)
//     * @throws Exception
//     */
//    public static String queryForecast(Point point) throws Exception {
//        String forecastURLStr = queryGridpoint(point);
//        String response = queryAPI(forecastURLStr, true);
//
//        return response;
//    }
//
//    /**
//     * @param point -- a (lat,lon) point
//     * @return a URL in the form of: "https://api.weather.gov/gridpoints/{gridpointCode}/{x},{y}/forecast"
//     * @throws Exception
//     */
//    private static String queryGridpoint(Point point) throws Exception {
//        String urlStr = String.format("%s/points/%d%%2C%d", NSW_API_URL, point.getLat(),point.getLon());
//        String response = queryAPI(urlStr, false);
//
//        int forecastURLBeginIndex = response.indexOf("\"forecast\": \"") + 13;
//        int forecastURLEndIndex = response.indexOf("\"forecastHourly\"") - 10;
//        String forecastURLStr = response.substring(forecastURLBeginIndex, forecastURLEndIndex);
//        return forecastURLStr;
//    }