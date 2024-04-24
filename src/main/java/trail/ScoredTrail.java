package trail;

import org.apache.spark.sql.Row;

public class ScoredTrail {
    
    public String feature_id;
    public String[] names;
    
    public int similarity;
    public double distance;

    public ScoredTrail(Row row, double distance) {
        this.feature_id = (String) row.getAs("feature_id");
        this.similarity = (Integer) row.getAs("similarity");
        this.names = new String[]{
            row.getAs("name"),
			row.getAs("name_1"),
			row.getAs("name_2"),
			row.getAs("name_3")
        };
        this.distance = distance;
    }

    public double getDistance() {
        return distance;
    }
}
