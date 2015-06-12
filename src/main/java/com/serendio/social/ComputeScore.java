package com.serendio.social;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import com.serendio.social.BaseScoreCalculator;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import org.codehaus.jackson.map.ObjectMapper;
//import com.serendio.categotyfinder.TweetCategoryModel;
//import com.serendio.categotyfinder.TweetPaser;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

public class ComputeScore {

	// Required to be read from config files.
	public static final String GLOBALLOGVALUE = "globalLogValue";
	public static final String ACCESS_KEY = "AKIAJVTLIV56HEEFIDIA";
	public static final String SECRET_KEY = "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";
	//public static final String DYNAMO_HOST = "http://localhost:8000";
	public static final String DYNAMO_HOST = "https://dynamodb.us-east-1.amazonaws.com/";
	public static final String DYNAMO_NAME = "dynamo";
	public static final String DYNAMO_REGION = "us-east-1";
	//public static get
	
	static AWSCredentials credentials = new BasicAWSCredentials(ACCESS_KEY,
			SECRET_KEY);
	static AmazonDynamoDBClient dynamoDBClient = new AmazonDynamoDBClient(
			credentials);// static String tableName = "user_score1";

	public static class MaxLogMapper extends
			Mapper<Object, Text, Text, DoubleWritable> {
		public void map(Object key, Text value, Context context)
				throws IOException, InterruptedException {
			String jsonString = value.toString();
			Map<String, Object> responseObject;
			try{
			ObjectMapper mapper = new ObjectMapper();
			responseObject = mapper.readValue(jsonString, Map.class);
 			//responseObject = new Gson().fromJson(
			//		jsonString, new TypeToken<Map<String, Object>>() {
			//		}.getType());
		}
			catch(Exception e){
				System.out.println(e.toString());
				System.out.println("No Json found");
				return;
			}		
			float avgPostFavCount = Float.parseFloat(responseObject.get(
					"avg_fav_count").toString());
			float avgPostRetweetCount = Float.parseFloat(responseObject.get(
					"avg_retweet_count").toString());
			long netFollowersCount = (long) Double.parseDouble(responseObject
					.get("net_followers_count").toString());
			long postCount = (long) Double.parseDouble(responseObject.get(
					"post_count").toString());
			float engagement = avgPostFavCount + avgPostRetweetCount;
			long reach = postCount * netFollowersCount;
			double tempScore = reach + 2 * engagement;
			if (tempScore<1){
				tempScore = 1;
			}
			double logValue = Math.log10(tempScore);
			context.write(new Text(GLOBALLOGVALUE), new DoubleWritable(logValue));
			List<Object> posts = (List<Object>)((Map<String, Object>) responseObject.get("contents")).get("posts");
			Map<String,Long> categoryPostCount = new HashMap<String, Long>();
			Map<String,Long> categoryEngagement = new HashMap<String, Long>();
			for(int i=0;i<posts.size();i++){
				Map<String, Object> post = (Map<String, Object>) posts.get(i);
				String text = post.get("text").toString();
				long favCount = (long)Double.parseDouble(post.get("favorite_count").toString());
				long retweetCount = (long) Double.parseDouble(post.get("retweet_count").toString());
				long postEngagent = favCount + retweetCount; 
				//TweetCategoryModel tm =  tweetParser.tweetClassifier(text);
				ArrayList<String> categories = (ArrayList<String>) post.get("category");
				//Set<String> categories = tm.getCategories();
				Iterator<String> categoryIterator = categories.iterator();
				while(categoryIterator.hasNext()){
					String category = categoryIterator.next();
					if(categoryPostCount.containsKey(category)){						
						categoryPostCount.put(category, categoryPostCount.get(category) + 1);						
					}
					else{
						
						categoryPostCount.put(category,(long) 1);
					}
					if(categoryEngagement.containsKey(category)){
						
						categoryEngagement.put(category, categoryEngagement.get(category)+ postEngagent);
					}
					else{
						categoryEngagement.put(category,postEngagent);
					}
				}
			}
			Iterator categoryEngagementIterator = categoryEngagement.entrySet().iterator();
			while(categoryEngagementIterator.hasNext()){
				Map.Entry<String, Long> pair =  (Map.Entry) categoryEngagementIterator.next();
				long totalEngamentCount = pair.getValue();
				double avgEngagementCount = totalEngamentCount / categoryPostCount.get(pair.getKey());
				double tempEngamentScore = (2 * avgEngagementCount);
				if (avgEngagementCount <1){
					tempEngamentScore = 1;
				}
				double categoricalLogValue = Math.log10(tempEngamentScore ) ;
				context.write(new Text(pair.getKey()), new DoubleWritable(categoricalLogValue));
			}
			
		}
	}

	public static class MaxLogReducer extends
			Reducer<Text, DoubleWritable, Text, DoubleWritable> {

		public void reduce(Text key, Iterable<DoubleWritable> values,
				Context context) throws IOException, InterruptedException {
			double maxValue = 0;
			for (DoubleWritable val : values) {
				double logVal = val.get();
				if (logVal > maxValue) {
					maxValue = logVal;
				}

			}
			context.write(key, new DoubleWritable(maxValue));
		}
	}

	public static class ScoringMapper extends Mapper<Object, Text, Text, Text> {


		public void map(Object key, Text value, Context context)
				throws IOException, InterruptedException {
			
			Configuration conf = context.getConfiguration();
			Double maxLogValue = Double.parseDouble(conf.get(GLOBALLOGVALUE).toString());
			String[] categoriesAvailable = conf.get("categories").split(",");
			Map<String, Double> categoricalLogValues = new HashMap<String,Double>();
			for(int i=0;i<categoriesAvailable.length;i++){
				String category = categoriesAvailable[i].trim();
				if(!category.isEmpty()){
					String s = conf.get(category);
					categoricalLogValues.put(category, Double.parseDouble(s));
				}
			}
			String jsonString = value.toString();
			
			Map<String, Object> responseObject;
			try{
			ObjectMapper mapper = new ObjectMapper();
			responseObject = mapper.readValue(jsonString, Map.class);
//			Map<String, Object> responseObject = new Gson().fromJson(
//					jsonString, new TypeToken<Map<String, Object>>() {
//					}.getType());

			}
			catch(Exception e){
				System.out.println(e.toString());
				System.out.println("No Json found");
				return;
			}
			int userId = (int) Double.parseDouble(responseObject.get("user_id")
					.toString());
			int pullTs = (int) Double.parseDouble(responseObject.get("pull_ts")
					.toString());			
			int connectionId = (int) Double.parseDouble(responseObject.get(
					"connection_id").toString());
			float avgPostFavCount = Float.parseFloat(responseObject.get(
					"avg_fav_count").toString());
			float avgPostRetweetCount = Float.parseFloat(responseObject.get(
					"avg_retweet_count").toString());
			long netFollowersCount = (long) Double.parseDouble(responseObject
					.get("net_followers_count").toString());
			long postCount = (long) Double.parseDouble(responseObject.get(
					"post_count").toString());
			String networkType = responseObject.get("network_type").toString();
			int networkId = (int) Double.parseDouble(responseObject.get(
					"network_id").toString());			
			float engagement = avgPostFavCount + avgPostRetweetCount;
			long reach = postCount * netFollowersCount;
			System.out.println(postCount * netFollowersCount);
			double score = BaseScoreCalculator.compute(reach, engagement,
					maxLogValue);
			List<Object> posts = (List<Object>)((Map<String, Object>) responseObject.get("contents")).get("posts");
			Map<String,Long> categoryPostCount = new HashMap<String, Long>();
			Map<String,Long> categoryEngagement = new HashMap<String, Long>();
			Map<String,Double> categoricalScore = new HashMap<String, Double>();
			for(int i=0;i<posts.size();i++){
				Map<String, Object> post = (Map<String, Object>) posts.get(i);				
				long favCount = (long)Double.parseDouble(post.get("favorite_count").toString());
				long retweetCount = (long) Double.parseDouble(post.get("retweet_count").toString());
				long postEngagent = favCount + retweetCount;				
				ArrayList<String> categories = (ArrayList<String>) post.get("category");
				Iterator<String> categoryIterator = categories.iterator();
				while(categoryIterator.hasNext()){
					String category = categoryIterator.next();
					if(categoryPostCount.containsKey(category)){						
						categoryPostCount.put(category, categoryPostCount.get(category) + 1);						
					}
					else{						
						categoryPostCount.put(category,(long) 1);
					}
					if(categoryEngagement.containsKey(category)){
						
						categoryEngagement.put(category, categoryEngagement.get(category)+ postEngagent);
					}
					else{
						categoryEngagement.put(category,(long) postEngagent);
					}
				}
			}
			Iterator categoryEngagementIterator = categoryEngagement.entrySet().iterator();
			while(categoryEngagementIterator.hasNext()){
				Map.Entry<String, Long> pair =  (Map.Entry) categoryEngagementIterator.next();
				long totalEngamentCount = pair.getValue();
				float engagementCount = (float)(totalEngamentCount / categoryPostCount.get(pair.getKey()));
				double categoryScore = BaseScoreCalculator.computePostsLevel(engagementCount, 
						categoricalLogValues.get(pair.getKey()));
				categoricalScore.put(pair.getKey(), new Double(new DecimalFormat("#.##").format(categoryScore)));
			}
			System.out.println(categoricalScore.toString());
			String name = responseObject.get("name").toString();
			List<String> categoryList = new ArrayList<String>(categoricalScore.keySet());
			dynamoDBClient.setEndpoint(DYNAMO_HOST, DYNAMO_NAME,
					DYNAMO_REGION);
			DynamoDB dynamoDB = new DynamoDB(dynamoDBClient);
			Table table = dynamoDB.getTable("user-score");
			try {
				Item item = new Item().withPrimaryKey("user_id", userId)
						.withNumber("connection_id", connectionId)
						.withString("network_type", networkType)
						.withString("name", name)
						.withNumber("network_id", networkId)
						.withNumber("pull_ts", pullTs)
						.withMap("topics_score", categoricalScore)
						.withList("topics", categoryList)
						.withNumber("score", score);
				table.putItem(item);

			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Error while putting into DynamoDB");
				System.out.println("Begins");
				System.out.println(categoricalScore);
				System.out.println(score);
				System.out.println("Ends");
			}
		}
	}

	public static void main(String[] args) throws Exception {

		Configuration conf = new Configuration();
		String fileSys = conf.get("fs.default.name");
		Job job = Job.getInstance(conf, "Izea Log Computation");
		job.setJarByClass(ComputeScore.class);
		job.setMapperClass(MaxLogMapper.class);
		job.setCombinerClass(MaxLogReducer.class);
		job.setReducerClass(MaxLogReducer.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(DoubleWritable.class);
		Path outputPath = new Path(args[1]);
		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, outputPath);
		if (job.waitForCompletion(true) ? true : false) {
			HashMap<String, Double> LogValueMap = new HashMap<String, Double>();
			try {
				Path distCache = new Path("/");
				
				distCache = new Path(args[1]);
				FileSystem fs = distCache.getFileSystem(conf); // for Amazon AWS
				if (fileSys.split(":")[0].trim().equalsIgnoreCase("s3n"))
					distCache = new Path("s3n:/" + distCache);
				Path pathPattern = new Path(distCache, "part-r-[0-9]*");
				FileStatus[] list = fs.globStatus(pathPattern);
				for (FileStatus status : list) {				
					try {
						String outputFile = status.getPath().toString();
						Path path = new Path(outputFile);
						FSDataInputStream fsin = fs.open(path);
						DataInputStream in = new DataInputStream(fsin);
						BufferedReader brr = new BufferedReader(
								new InputStreamReader(in));
						String line;
						while ((line = brr.readLine()) != null) {
							String[] resultsCount = line.split("\t");
							LogValueMap.put(resultsCount[0],
									Double.parseDouble(resultsCount[1].trim()));
						}
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			} catch (Exception e) {
			}
			Iterator<Entry<String, Double>> logValueIterator = LogValueMap.entrySet().iterator();
			String categoryAvailable  = "";
			while(logValueIterator.hasNext()){
				Entry<String, Double> pair = logValueIterator.next();
				categoryAvailable = categoryAvailable + "," + pair.getKey();
				conf.set(pair.getKey(), pair.getValue().toString().trim());
			}
			conf.set("categories", categoryAvailable);
			if(LogValueMap.get(GLOBALLOGVALUE)!=null){
				conf.set(GLOBALLOGVALUE, LogValueMap.get(GLOBALLOGVALUE).toString());
			}
			else{
				System.exit(0);
			}
			Job scoringJob = Job.getInstance(conf, "Izea Log Computation");
			scoringJob.setJarByClass(ComputeScore.class);
			scoringJob.setMapperClass(ScoringMapper.class);
			scoringJob.setOutputKeyClass(Text.class);			
			scoringJob.setOutputValueClass(DoubleWritable.class);
			Path ScoreoutputPath = new Path(args[2]);
			FileInputFormat.addInputPath(scoringJob, new Path(args[0]));
			FileOutputFormat.setOutputPath(scoringJob, ScoreoutputPath);
			System.exit(scoringJob.waitForCompletion(true) ? 0:1); 			
		} else {
			System.exit(1);
		}
	}
}