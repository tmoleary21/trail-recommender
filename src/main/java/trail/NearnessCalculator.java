package trail;

import java.io.Serializable;
import java.util.List;

public class NearnessCalculator implements Serializable {

    private final double refLatitude;
    private final double refLongitude;

    public NearnessCalculator(double refLatitude, double refLongitude) {
        this.refLatitude = refLatitude;
        this.refLongitude = refLongitude;
    }

    public double calculateNearness(List<List<Double>> coordinates) {
        double minDistance = Double.MAX_VALUE;
        for (List<Double> coordinate : coordinates) {
            double distance = greatCircleDistance(coordinate);
            if (distance < minDistance) {
                minDistance = distance;
            }
        }
        return minDistance;
    }

    // Below are adapted from my 314 server code
    private double greatCircleDistance(List<Double> coord) {
        // Coordinates are [longitude, latitude]
        double dlambda = coord.get(0) - refLongitude;
        double centralAngle = vincentyDegrees(coord.get(1), refLatitude, dlambda);
        return centralAngle; // Multiply by a radius to get arc length, but not necessary for our comparisons
    }

    private double vincentyRadians(double phi1, double phi2, double dlambda) {
        double cosphi1 = Math.cos(phi1);
        double sinphi1 = Math.sin(phi1);
        double cosphi2 = Math.cos(phi2);
        double sinphi2 = Math.sin(phi2);
        double sindlambda = Math.sin(dlambda);
        double cosdlambda = Math.cos(dlambda);
        double square1 = Math.pow(cosphi2 * sindlambda, 2);
        double square2 = Math.pow((cosphi1 * sinphi2) - (sinphi1 * cosphi2 * cosdlambda), 2);
        double numerator = Math.sqrt(square1 + square2);
        double denominator = (sinphi1 * sinphi2) + (cosphi1 * cosphi2 * cosdlambda);
        double centralAngle = Math.atan2(numerator, denominator);
        return centralAngle;
    }

    // Parameters are in degrees. Returns radians
    private double vincentyDegrees(double phi1, double phi2, double dlambda) {
        return vincentyRadians(Math.toRadians(phi1),
                Math.toRadians(phi2),
                Math.toRadians(dlambda));
    }

}
