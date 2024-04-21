package trail;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.*;

public class QueryFlask {
    public static final String FLASK_SERVER = "http://phoenix:6000/forecast";

    public static Forecast queryFlaskServer(String serverUrl, Point point) throws Exception {
        URL flaskURL = new URL(serverUrl);
        HttpURLConnection connection = (HttpURLConnection) flaskURL.openConnection();
        connection.setDoOutput(true);

        connection.setRequestMethod("POST");
        connection.addRequestProperty("Content-Type", "application/" + "json");
        String query = String.format("{\"lat\": %d, \"lon\": %d}", point.getLat(), point.getLon());
        connection.setRequestProperty("Content-Length", Integer.toString(query.length()));
        connection.getOutputStream().write(query.getBytes("UTF-8"));

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

        String responseBody = content.toString();
        JSONObject jsonObject = new JSONObject(responseBody);

        return new Forecast(jsonObject);
    }

    /* uncomment if you want a demonstration */
//    public static void main (String[] args) {
//        Point point = new Point(40, -105);
//        try {
//            Forecast forecast = queryFlaskServer(FLASK_SERVER, point);
//            System.out.println(forecast);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }
}
