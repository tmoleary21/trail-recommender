package trail;

public class Point {
    private int lat;
    private int lon;

    public Point(int lat, int lon) {
        this.lat = lat;
        this.lon = lon;
    }

    public int getLat() {
        return lat;
    }

    public int getLon() {
        return lon;
    }

    public void setLat(int lat) {
        this.lat = lat;
    }

    public void setLon(int lon) {
        this.lon = lon;
    }
}