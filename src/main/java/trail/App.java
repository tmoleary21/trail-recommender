package trail;

import java.util.HashMap;

import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

public class App {

    public static void main(String[] args) {

        SparkSession spark = SparkSession.builder()
                .appName("Simple Application")
                .getOrCreate();

        HashMap<String, String> options = new HashMap<String, String>() {
            {
                put("multiline", "true");
            }
        };

        Dataset<Row> trailsDataset = spark.read().options(options)
                .json("/s/bach/n/under/tmoleary/cs555/term-project/data/cpw_trails/json/Trails_COTREX02072024.json");

        trailsDataset.printSchema();
        // trailsDataset.explain();

        Dataset<Row> trailheadsDataset = spark.read().options(options).json(
                "/s/bach/n/under/tmoleary/cs555/term-project/data/cpw_trails/json/Trailheads_COTREX02072024.json");

        trailheadsDataset.printSchema();
        // trailheadsDataset.explain();

        Dataset<Row> designatedTrailsDataset = spark.read().options(options).json(
                "/s/bach/n/under/tmoleary/cs555/term-project/data/cpw_trails/json/CPWDesignatedTrails02072024.json");

        designatedTrailsDataset.printSchema();
        // designatedTrailsDataset.explain();

    }

}
