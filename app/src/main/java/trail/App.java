package trail;

import org.apache.spark.sql.SparkSession;

public class App {

    public static void main(String[] args) {

        SparkSession spark = SparkSession.builder().appName("trail-recommender").getOrCreate();

        // spark.read()

    }

}
