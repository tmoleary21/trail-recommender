package trail;

import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.api.java.UDF1;
import org.apache.spark.sql.types.DataTypes;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.api.java.function.FilterFunction;
import org.apache.spark.api.java.function.ReduceFunction;
import org.apache.spark.ml.feature.OneHotEncoder;
import org.apache.spark.ml.feature.OneHotEncoderModel;
import org.apache.spark.ml.feature.StringIndexer;
import org.apache.spark.ml.feature.StringIndexerModel;
import org.apache.spark.ml.feature.VectorAssembler;
import org.apache.spark.ml.linalg.DenseVector;
import org.apache.spark.ml.linalg.SparseVector;
import org.apache.spark.sql.expressions.UserDefinedFunction;

import static org.apache.spark.sql.functions.expr;
import static org.apache.spark.sql.functions.udf;

import java.util.Arrays;

import static org.apache.spark.sql.functions.abs;
import static org.apache.spark.sql.functions.avg;

import org.apache.sedona.spark.SedonaContext;

public class App {

	private static final String[] identifierFields = {
			"feature_id",
			"name",
			"name_1",
			"name_2",
			"name_3"
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
			//"place_id",
			"place_id_1",
			"place_id_2",
			"place_id_3"
	};

	// options and booleans
	private static final String[] allStringFields = {
			"access", "manager", "seasonal_1", "seasonal_2", "seasonal_3", "seasonalit", "surface", "type", "atv",
			"bike", "dogs", "groomed", "highway_ve", "hiking", "horse", "motorcycle", "ohv_gt_50", "oneway",
			"plowed", "ski", "snowmobile", "snowshoe"
	};

    private static final String[] dropStringFields = {
            "manager", "seasonal_1", "seasonal_2", "seasonal_3", "seasonalit", "type", "atv",
            "groomed", "highway_ve", "motorcycle", "ohv_gt_50", "oneway",
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

	private static final String trailPropertiesSchema = "struct<"
			+"EDIT_DATE string,"
			+"INPUT_DATE string,"
			+"access string,"
			+"atv string,"
			+"bike string,"
			+"dogs string,"
			+"feature_id string,"
			+"groomed string,"
			+"groomer_ur string,"
			+"highway_ve string,"
			+"hiking string,"
			+"horse string,"
			+"length_mi_ double,"
			+"manager string,"
			+"max_elevat double,"
			+"min_elevat double,"
			+"motorcycle string,"
			+"name string,"
			+"name_1 string,"
			+"name_2 string,"
			+"name_3 string,"
			+"ohv_gt_50 string,"
			+"oneway string,"
			+"place_id long,"
			+"place_id_1 long,"
			+"place_id_2 long,"
			+"place_id_3 long,"
			+"plowed string,"
			+"seasonal_1 string,"
			+"seasonal_2 string,"
			+"seasonal_3 string,"
			+"seasonalit string,"
			+"ski string,"
			+"snowmobile string,"
			+"snowshoe string,"
			+"surface string,"
			+"trail_nu_1 string,"
			+"trail_num string,"
			+"trail_num1 string,"
			+"trail_num_ string,"
			+"type string,"
			+"url string"
			+">";

	public static void main(String[] args) {

		try (SparkSession spark = SedonaContext.builder()
				.appName("Trail recommender")
				.getOrCreate();) {
					
			run(spark, args);

		}

	}

	private static void run(SparkSession spark, String[] trailQueries) {

		// User inputs (temporary) --------------

		// String[] trailQueries = { "Horsetooth Rock Trail", "Arthur's Rock Trail" }; // place_id 13275, 13875 resp.

		double locationLatitude = 40.575405;
		double locationLongitude = -105.084648;

		// ---------------------------------------

		// Only works because the CS machines share our home directories
		// multiline option needed to read json that's not in json lines format
		String schema = "type string, crs string, features array<struct<type string, geometry string, properties "+trailPropertiesSchema+">>";
		Dataset<Row> trailsDataset = spark.read().schema(schema)
			.option("multiLine", true).json("/s/bach/n/under/tmoleary/cs555/term-project/data/raw/cpw_trails/json/Trails_COTREX02072024.json")
			.selectExpr("explode(features) as features") // Explode the envelope to get one feature per row.
			.select("features.*") // Unpack the features struct.
			.select("properties.*", "geometry") // Flatten
			.drop(irrelevantFields);

		trailsDataset = trailsDataset
			.filter(trailsDataset.col("geometry").isNotNull()) // Remove null geometries
			.withColumn("geometry", expr("ST_GeomFromGeoJSON(geometry)")) // Convert the geometry string.
			.withColumn("geometry", expr("ST_TRANSFORM(geometry, 'EPSG:26913','EPSG:4326')")); // Convert CRS to EPSG:4326


		Dataset<Row> vectorized = vectorize(trailsDataset);
		vectorized.persist();

		// Find all the trails matching user input
		Dataset<Row> queriedTrails = vectorized.filter((FilterFunction<Row>) row -> nameIncluded(trailQueries, row));

		trailsDataset = calculateSimilarityScores(spark, vectorized, queriedTrails);
		vectorized.unpersist();

		String userLocationSql = "ST_GeomFromText('Point("+locationLongitude+" "+locationLatitude+")')";
		trailsDataset = trailsDataset
			.withColumn("centroid", expr("ST_Centroid(geometry)"))
			.withColumn("distance", expr("ST_DistanceSphere(centroid, "+userLocationSql+")"))
			.drop("geometry");


		// numeric
		Row averages = queriedTrails.select(
			avg(queriedTrails.col("length_mi_")).as("length"), 
			avg(queriedTrails.col("max_elevat").minus(queriedTrails.col("min_elevat")).as("elevation"))
		).takeAsList(1).get(0);

		double averageLength = averages.getDouble(0);
		double averageElevation = averages.getDouble(1);

		Dataset<Row> similarCandidates = trailsDataset.sort(
			trailsDataset.col("similarity").desc()
		).limit(1000);
		similarCandidates = similarCandidates.sort(
			abs(similarCandidates.col("length_mi_")
				.minus(averageLength)
			).asc()
		).limit(100);
		similarCandidates = similarCandidates.sort(
			abs(similarCandidates.col("max_elevat")
				.minus(similarCandidates.col("min_elevat"))
				.minus(averageElevation)
			).asc()
		).limit(50);
		Dataset<Row> closestSimilar = similarCandidates.sort(
			similarCandidates.col("distance").asc()
		).limit(10);

		closestSimilar = closestSimilar
			//.drop(numericFields)
			.withColumn("centroid", expr("ST_AsGeoJSON(centroid)"));

		closestSimilar.write()
			.mode("overwrite")
			.json("/s/bach/n/under/tmoleary/cs555/term-project/data/output"); // Can't name file. Saved as hadoop part filename in json lines format
			
	}

	private static Dataset<Row> calculateSimilarityScores(SparkSession spark, Dataset<Row> vectorized, Dataset<Row> queriedTrails) {
		// Merge to one vector
		SparseVector query = mergeTrailVectors(queriedTrails);

		// Define similarity score function
		UserDefinedFunction score = udf(
				(UDF1<SparseVector, Double>) (SparseVector vec) -> vec.dot(query), DataTypes.DoubleType
		);
		spark.udf().register("score", score);

		// Calculate similarity score for all trails
		vectorized.createOrReplaceTempView("vectorized");
		Dataset<Row> similarityScores = spark.sql("SELECT *, score(vector) as similarity FROM vectorized");
		similarityScores = similarityScores.drop("vector");
		return similarityScores;
	}

	private static SparseVector mergeTrailVectors(Dataset<Row> queriedTrails) {
		Dataset<Row> justVectors = queriedTrails.select("vector");
		Row reducedRow = justVectors.reduce((ReduceFunction<Row>) App::addRowVectors);
		return (SparseVector) reducedRow.get(0);
	}

	private static Row addRowVectors(Row row1, Row row2) {
		SparseVector v1 = (SparseVector) row1.get(0); // Can't do string index without a schema
		SparseVector v2 = (SparseVector) row2.get(0); // Also can't figure out how to add schema
		return RowFactory.create(addVectors(v1, v2));
	}

	private static SparseVector addVectors(SparseVector v1, SparseVector v2) {
		double[] sumVec = v1.toArray();
		int[] indices2 = v2.indices();
		double[] values2 = v2.values();
		for (int i = 0; i < indices2.length; i++) {
			sumVec[indices2[i]] += values2[i];
		}
		return new DenseVector(sumVec).toSparse();
	}

	private static boolean nameIncluded(String[] trailQueries, Row row) {
		return contains(trailQueries, row.getAs("name"))
				|| contains(trailQueries, row.getAs("name_1"))
				|| contains(trailQueries, row.getAs("name_2"))
				|| contains(trailQueries, row.getAs("name_3"));
	}

	private static boolean contains(String[] items, String query) {
		for (String item : items) {
			if (item.equals(query)) {
				return true;
			}
		}
		return false;
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

		Dataset<Row> cleaned = withVector.drop(dropStringFields);
		cleaned = cleaned.drop(indexedFields);
		cleaned = cleaned.drop(encodedFields);

		return cleaned;
	}

}
