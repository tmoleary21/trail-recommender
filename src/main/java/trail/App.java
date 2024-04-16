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
import org.apache.spark.ml.feature.StringIndexer;
import org.apache.spark.ml.feature.StringIndexerModel;
import org.apache.spark.ml.feature.VectorAssembler;
import org.apache.spark.sql.Encoders;

import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.SparkConf;

public class App {

	static final String[] optionFields = {
			"access", "manager", "seasonal_1", "seasonal_2", "seasonal_3", "seasonalit", "surface", "type"
	};
	static final String[] indexedFields = {
			"accessI", "managerI", "seasonal_1I", "seasonal_2I", "seasonal_3I", "seasonalitI", "surfaceI", "typeI"
	};
	static final String[] encodedFields = {
			"accessE", "managerE", "seasonal_1E", "seasonal_2E", "seasonal_3E", "seasonalitE", "surfaceE", "typeE"
	};

	public static void main(String[] args) {

		SparkSession spark = SparkSession.builder()
				.appName("Trail Recommender")
				.getOrCreate();

		Dataset<Row> trailsDataset = spark.read()
				.json("/s/bach/n/under/tmoleary/cs555/term-project/data/trails/Trails_COTREX02072024.jsonl");

		trailsDataset.printSchema();

		trailsDataset.show(4);

		// Only way I could figure to get properties out without making huge schema
		trailsDataset.createOrReplaceTempView("trails");
		Dataset<Row> properties = spark.sql("SELECT properties.* FROM trails");

		properties.show(4);
		properties.printSchema();

		StringIndexer indexer = new StringIndexer()
				.setInputCols(optionFields)
				.setOutputCols(indexedFields)
				.setHandleInvalid("keep"); // Keep null values as the final index
		StringIndexerModel indexerModel = indexer.fit(properties);
		Dataset<Row> indexedProperties = indexerModel.transform(properties);

		// Because we kept null values in the StringIndexer, we will keep the default
		// dropLast=true for the encoder.
		// This way, nulls are encoded as a vector of all zeros
		OneHotEncoder encoder = new OneHotEncoder()
				.setInputCols(indexedFields)
				.setOutputCols(encodedFields);
		OneHotEncoderModel model = encoder.fit(indexedProperties);
		Dataset<Row> encoded = model.transform(indexedProperties);

		encoded.show(4);

		VectorAssembler assembler = new VectorAssembler()
				.setInputCols(encodedFields)
				.setOutputCol("vector");
		Dataset<Row> withVector = assembler.transform(encoded);

		Dataset<Row> cleaned = withVector.drop(optionFields);
		cleaned = cleaned.drop(indexedFields);
		cleaned = cleaned.drop(encodedFields);

		cleaned.show(10);

		spark.stop();

	}

}
