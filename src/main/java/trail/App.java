package trail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.sql.rowset.RowSetFactory;

import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.api.java.UDF1;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.NumericType;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.locationtech.jts.geom.Geometry;
import org.sparkproject.dmg.pmml.DataType;

import com.google.protobuf.Struct;

import scala.collection.mutable.StringBuilder;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.api.java.function.FilterFunction;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.ForeachFunction;
import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.api.java.function.ReduceFunction;
import org.apache.spark.ml.feature.OneHotEncoder;
import org.apache.spark.ml.feature.OneHotEncoderModel;
import org.apache.spark.ml.feature.StringIndexer;
import org.apache.spark.ml.feature.StringIndexerModel;
import org.apache.spark.ml.feature.VectorAssembler;
import org.apache.spark.ml.linalg.DenseVector;
import org.apache.spark.ml.linalg.SparseVector;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.expressions.UserDefinedFunction;

import static org.apache.spark.sql.functions.expr;
import static org.apache.spark.sql.functions.udf;

import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.JavaRDD;
import org.apache.sedona.core.formatMapper.shapefileParser.ShapefileReader;
import org.apache.sedona.core.spatialRDD.SpatialRDD;
import org.apache.sedona.core.utils.SedonaConf;
import org.apache.sedona.spark.SedonaContext;
import org.apache.sedona.sql.utils.Adapter;
import org.apache.sedona.sql.utils.SedonaSQLRegistrator;
import org.apache.spark.SparkConf;

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
			"place_id",
			"place_id_1",
			"place_id_2",
			"place_id_3"
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

		try (SparkSession spark = SedonaContext.builder()
				.appName("Trail recommender")
				.getOrCreate();) {

			run(spark);

		}

	}

	private static void run(SparkSession spark) {

		// User inputs (temporary) --------------

		String[] trailQueries = { "Horsetooth Rock Trail", "Arthur's Rock Trail" }; // place_id 13275, 13875 resp.

		double locationLatitude = 40.575405;
		double locationLongitude = -105.084648;

		// ---------------------------------------

		String trailPropertiesSchema = "struct<"
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

		// StructType trailPropertiesSchema = new StructType(new StructField[] {
		// 	new StructField("EDIT_DATE", DataTypes.StringType, true, null),
		// 	new StructField("INPUT_DATE", DataTypes.StringType, true, null),
		// 	new StructField("access", DataTypes.StringType, true, null),
		// 	new StructField("atv", DataTypes.StringType, true, null),
		// 	new StructField("bike", DataTypes.StringType, true, null),
		// 	new StructField("dogs", DataTypes.StringType, true, null),
		// 	new StructField("feature_id", DataTypes.StringType, true, null),
		// 	new StructField("groomed", DataTypes.StringType, true, null),
		// 	new StructField("groomer_ur", DataTypes.StringType, true, null),
		// 	new StructField("highway_ve", DataTypes.StringType, true, null),
		// 	new StructField("hiking", DataTypes.StringType, true, null),
		// 	new StructField("horse", DataTypes.StringType, true, null),
		// 	new StructField("length_mi_", DataTypes.DoubleType, true, null),
		// 	new StructField("manager", DataTypes.StringType, true, null),
		// 	new StructField("max_elevat", DataTypes.DoubleType, true, null),
		// 	new StructField("min_elevat", DataTypes.DoubleType, true, null),
		// 	new StructField("motorcycle", DataTypes.StringType, true, null),
		// 	new StructField("name", DataTypes.StringType, true, null),
		// 	new StructField("name_1", DataTypes.StringType, true, null),
		// 	new StructField("name_2", DataTypes.StringType, true, null),
		// 	new StructField("name_3", DataTypes.StringType, true, null),
		// 	new StructField("ohv_gt_50", DataTypes.StringType, true, null),
		// 	new StructField("oneway", DataTypes.StringType, true, null),
		// 	new StructField("place_id", DataTypes.LongType, true, null),
		// 	new StructField("place_id_1", DataTypes.LongType, true, null),
		// 	new StructField("place_id_2", DataTypes.LongType, true, null),
		// 	new StructField("place_id_3", DataTypes.LongType, true, null),
		// 	new StructField("plowed", DataTypes.StringType, true, null),
		// 	new StructField("seasonal_1", DataTypes.StringType, true, null),
		// 	new StructField("seasonal_2", DataTypes.StringType, true, null),
		// 	new StructField("seasonal_3", DataTypes.StringType, true, null),
		// 	new StructField("seasonalit", DataTypes.StringType, true, null),
		// 	new StructField("ski", DataTypes.StringType, true, null),
		// 	new StructField("snowmobile", DataTypes.StringType, true, null),
		// 	new StructField("snowshoe", DataTypes.StringType, true, null),
		// 	new StructField("surface", DataTypes.StringType, true, null),
		// 	new StructField("trail_nu_1", DataTypes.StringType, true, null),
		// 	new StructField("trail_num", DataTypes.StringType, true, null),
		// 	new StructField("trail_num1", DataTypes.StringType, true, null),
		// 	new StructField("trail_num_", DataTypes.StringType, true, null),
		// 	new StructField("type", DataTypes.StringType, true, null),
		// 	new StructField("url", DataTypes.StringType, true, null)
		// });

		StructType featureSchema = new StructType(new StructField[] {
			new StructField("type", DataTypes.StringType, true, null),
			new StructField("properties", DataTypes.StringType, true, null),
			new StructField("geometry", DataTypes.StringType, true, null) // Important this is a string
			// new StructField("properties", trailPropertiesSchema, true, null)
		});

		StructType featureCollectionSchema = new StructType(new StructField[] {
			new StructField("name", DataTypes.StringType, true, null),
			new StructField("type", DataTypes.StringType, true, null),
			new StructField("crs", DataTypes.StringType, true, null),
			new StructField("features", DataTypes.StringType/*DataTypes.createArrayType(featureSchema)*/, true, null)
		});

		// Only works because the CS machines share our home directories
		// multiline option needed to read json that's not in json lines format
		String schema = "type string, crs string, features array<struct<type string, geometry string, properties "+trailPropertiesSchema+">>";
		Dataset<Row> trailsDataset = spark.read().schema(schema)
			.option("multiLine", true).json("/s/bach/n/under/tmoleary/cs555/term-project/data/raw/cpw_trails/json/Trails_COTREX02072024.json")
			// .json("/s/bach/n/under/tmoleary/cs555/term-project/data/trails/Trails_COTREX02072024.jsonl")
			.selectExpr("explode(features) as features") // Explode the envelope to get one feature per row.
			.select("features.*") // Unpack the features struct.
			.select("properties.*", "geometry") // Flatten
			.drop(irrelevantFields);
			
		Dataset<Row> geometry = trailsDataset
			.select("feature_id", "geometry")
			.filter(trailsDataset.col("geometry").isNotNull())
			.withColumn("geometry", expr("ST_GeomFromGeoJSON(geometry)")) // Convert the geometry string.
			.withColumn("geometry", expr("ST_TRANSFORM(geometry, 'EPSG:26913','EPSG:4326')")); // Convert CRS to EPSG:4326

		// geometry.printSchema();
		
		Dataset<Row> properties = trailsDataset.drop("geometry");

		// properties.printSchema();

		// trailsDataset.printSchema();
		// trailsDataset.show(1);

		properties = calculateSimilarityScores(spark, properties, trailQueries);
		// properties.printSchema();
		// properties.show(10);

		List<Row> sparkCandidates = properties.sort(properties.col("similarity").desc()).takeAsList(50);
		// System.out.println(sparkCandidates);

		
		String userLocationSql = "ST_GeomFromText('Point("+locationLongitude+" "+locationLatitude+")')";
		geometry = geometry.withColumn("distance", expr("ST_DistanceSphere(geometry, "+userLocationSql+")"))
			.drop("geometry");
		
		// geometry.printSchema();
		// geometry.show((int)geometry.count());
		
		Map<String, Double> distanceMap = geometry
		.collectAsList()
		.stream()
		.collect(Collectors.toMap(
			item -> item.getString(0), 
			item -> item.getDouble(1)
			));
			
		List<ScoredTrail> candidates = sparkCandidates
			.stream()
			.map(item -> new ScoredTrail(item, (Double) distanceMap.get((String) item.getAs("feature_id"))))
			.sorted(Comparator.comparing(ScoredTrail::getDistance))
			.limit(10)
			.collect(Collectors.toList());

		System.out.println(candidates);
		
			
	}

	private static Dataset<Row> calculateSimilarityScores(SparkSession spark, Dataset<Row> trailsDataset, String[] trailQueries) {
		Dataset<Row> vectorized = vectorize(trailsDataset);
		vectorized.persist();

		// Find all the trails matching user input
		Dataset<Row> queriedTrails = vectorized.filter((FilterFunction<Row>) row -> nameIncluded(trailQueries, row));
		// Merge to one vector
		SparseVector query = mergeTrailProperties(queriedTrails);

		// Define similarity score function
		UserDefinedFunction score = udf(
				(UDF1<SparseVector, Double>) (SparseVector vec) -> vec.dot(query), DataTypes.DoubleType
		);
		spark.udf().register("score", score);

		// Calculate similarity score for all trails
		vectorized.createOrReplaceTempView("vectorized");
		Dataset<Row> similarityScores = spark.sql("SELECT *, score(vector) as similarity FROM vectorized");
		similarityScores = similarityScores.drop("vector");
		vectorized.unpersist();
		return similarityScores;
	}

	private static SparseVector mergeTrailProperties(Dataset<Row> queriedTrails) {
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

		Dataset<Row> cleaned = withVector.drop(allStringFields);
		cleaned = cleaned.drop(indexedFields);
		cleaned = cleaned.drop(encodedFields);

		return cleaned;
	}

}
