package trail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.ml.feature.OneHotEncoder;
import org.apache.spark.ml.feature.OneHotEncoderModel;
import org.apache.spark.sql.Encoders;

public class App {

    public static void main(String[] args) {

        SparkSession spark = SparkSession.builder()
                .appName("Simple Application")
                .getOrCreate();

        Dataset<Row> trailsDataset = spark.read()
                .json("/s/bach/n/under/tmoleary/cs555/term-project/data/trails/Trails_COTREX02072024.jsonl");

        trailsDataset.printSchema();

        // StructType strut = new StructType(new StructField[] {
        // new StructField("q", null, false, null)
        // });

        trailsDataset.show(4);

        String[] optionFields = {
                "properties.access", "properties.manager", "properties.seasonal_1", "properties.seasonal_2",
                "properties.seasonal_3", "properties.seasonalit", "properties.surface", "properties.type"
        };
        String[] outputVectorFields = {
                "accessO", "managerO", "seasonal_1O", "seasonal_2O", "seasonal_3O", "seasonalitO", "surfaceO", "typeO"
        };
        OneHotEncoder encoder = new OneHotEncoder()
                .setInputCols(optionFields)
                .setOutputCols(outputVectorFields)
                .setDropLast(false);

        OneHotEncoderModel model = encoder.fit(trailsDataset);
        Dataset<Row> encoded = model.transform(trailsDataset);

        encoded.show();

        spark.stop();

    }

}
