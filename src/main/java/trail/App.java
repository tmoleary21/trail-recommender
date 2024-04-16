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

	private static final String[] identifierFields = {
			"feature_id", "name"
	};

	private static final String[] irrelevantFields = {
			"EDIT_DATE",
			"INPUT_DATE",
			"groomer_ur",
			"trail_nu_1",
			"trail_num",
			"trail_num1",
			"trail_num_",
			"url",
			"place_id",
			"place_id_1",
			"place_id_2",
			"place_id_3",
			"name_1",
			"name_2",
			"name_3"
	};

	private static final String[] optionFields = {
			"access", "manager", "seasonal_1", "seasonal_2", "seasonal_3", "seasonalit", "surface", "type"
	};

	private static final String[] booleanFields = {
			"atv", "bike", "dogs", "groomed", "highway_ve", "hiking", "horse", "motorcycle", "ohv_gt_50", "oneway",
			"plowed", "ski", "snowmobile", "snowshoe"
	};

	// options and booleans
	private static final String[] allStringFields = {
			"access", "manager", "seasonal_1", "seasonal_2", "seasonal_3", "seasonalit", "surface", "type", "atv",
			"bike", "dogs", "groomed", "highway_ve", "hiking", "horse", "motorcycle", "ohv_gt_50", "oneway",
			"plowed", "ski", "snowmobile", "snowshoe"
	};

	private static final String[] indexedFields = {
			"accessI", "managerI", "seasonal_1I", "seasonal_2I", "seasonal_3I", "seasonalitI", "surfaceI", "typeI",
			"atvI", "bikeI", "dogsI", "groomedI", "highway_veI", "hikingI", "horseI", "motorcycleI", "ohv_gt_50I",
			"onewayI", "plowedI", "skiI", "snowmobileI", "snowshoeI"
	};
	private static final String[] encodedFields = {
			"accessE", "managerE", "seasonal_1E", "seasonal_2E", "seasonal_3E", "seasonalitE", "surfaceE", "typeE",
			"atvE", "bikeE", "dogsE", "groomedE", "highway_veE", "hikingE", "horseE", "motorcycleE", "ohv_gt_50E",
			"onewayE", "plowedE", "skiE", "snowmobileE", "snowshoeE"
	};

	private static final String[] numericFields = { "length_mi_", "max_elevat", "min_elevat" };

	public static void main(String[] args) {

		try (SparkSession spark = SparkSession.builder()
				.appName("Trail Recommender")
				.getOrCreate();) {

			run(spark);

		}

	}

	private static void run(SparkSession spark) {
		Dataset<Row> trailsDataset = spark.read()
				.json("/s/bach/n/under/tmoleary/cs555/term-project/data/trails/Trails_COTREX02072024.jsonl");

		Dataset<Row> properties = replaceRoot(spark, trailsDataset, "properties");
		properties = properties.drop(irrelevantFields);

		Dataset<Row> numeric = properties.drop(allStringFields);

		Dataset<Row> categorical = properties.drop(numericFields);
		Dataset<Row> vectorized = vectorize(categorical);

		vectorized.show(10);
		numeric.show(10);
	}

	private static Dataset<Row> replaceRoot(SparkSession spark, Dataset<Row> df, String subStructField) {
		// Only way I could figure to get properties out without making huge schema
		df.createOrReplaceTempView("replacement_view");
		return spark.sql("SELECT " + subStructField + ".* FROM replacement_view");
	}

	private static Dataset<Row> vectorize(Dataset<Row> categoricalProperties) {
		StringIndexer indexer = new StringIndexer()
				.setInputCols(allStringFields)
				.setOutputCols(indexedFields)
				.setHandleInvalid("keep"); // Keep null values as the final index
		StringIndexerModel indexerModel = indexer.fit(categoricalProperties);
		Dataset<Row> indexedProperties = indexerModel.transform(categoricalProperties);

		// Because we kept null values in the StringIndexer, we will keep the default
		// dropLast=true for the encoder.
		// This way, nulls are encoded as a vector of all zeros
		OneHotEncoder encoder = new OneHotEncoder()
				.setInputCols(indexedFields)
				.setOutputCols(encodedFields);
		OneHotEncoderModel model = encoder.fit(indexedProperties);
		Dataset<Row> encoded = model.transform(indexedProperties);

		VectorAssembler assembler = new VectorAssembler()
				.setInputCols(encodedFields)
				.setOutputCol("vector");
		Dataset<Row> withVector = assembler.transform(encoded);

		Dataset<Row> cleaned = withVector.drop(allStringFields);
		cleaned = cleaned.drop(indexedFields);
		cleaned = cleaned.drop(encodedFields);

		return cleaned;
	}

}
