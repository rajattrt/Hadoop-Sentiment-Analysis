package sentiment;

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.hadoop.mapreduce.Job; 
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Counters;

public class SentAnalyser extends Configured implements Tool {
	
	public static void main(String[] args) throws Exception {
 int res = ToolRunner.run(new SentAnalyser(), args);
 System.exit(res);
}

//The run method configures and starts the MapReduce job.

	public int run(String[] args) throws Exception {
		Job job = Job.getInstance(getConf(), "sentanalyser");
			for (int i = 0; i < args.length; i += 1) {
				if ("-skip".equals(args[i])) {
					job.getConfiguration().setBoolean("sentanalyser.skip.patterns", true);
					i += 1;
					job.addCacheFile(new Path(args[i]).toUri());
				}
				if ("-no_case".equals(args[i])) {
					job.getConfiguration().setBoolean("sentanalyser.case.sensitive", true);
				}
				if ("-pos".equals(args[i])) {
					job.getConfiguration().setBoolean("sentanalyser.pos.patterns", true);
					i += 1;
					job.addCacheFile(new Path(args[i]).toUri());
				}    
				if ("-neg".equals(args[i])) {
					job.getConfiguration().setBoolean("sentanalyser.neg.patterns", true);
					i += 1;
					job.addCacheFile(new Path(args[i]).toUri());
				}
		}

	
	
				job.setJarByClass(this.getClass());
				FileInputFormat.addInputPath(job, new Path(args[0]));
				FileOutputFormat.setOutputPath(job, new Path(args[1]));
				job.setMapperClass(Map.class);
				job.setCombinerClass(Reduce.class);
				job.setReducerClass(Reduce.class);
				job.setOutputKeyClass(Text.class);
				job.setOutputValueClass(IntWritable.class);
				int result = job.waitForCompletion(true) ? 0 : 1;
		 
	
				Counters counters = job.getCounters();
				 float good = counters.findCounter("sentiment.Map$Gauge", "POSITIVE").getValue();
				 float bad = counters.findCounter("sentiment.Map$Gauge", "NEGATIVE").getValue();
		 
		 		if (good + bad > 0) {
				// Calculate the basic sentiment score by dividing the difference 
				// of good and bad words by their sum.
	
				float sentiment = ((good - bad) / (good + bad));
		
				// Calculate the positivity score by dividing good results by the sum of
				// good and bad results. Multiply by 100 and round off to get a percentage.
				// Results 50% and above are more positive, overall.		
	
				float positivity = (good / (good + bad))*100;
				int positivityScore = Math.round(positivity);
		
				// Display the results in the console.
	
				System.out.println("\n\n\n**********\n\n\n");
				System.out.println("Sentiment score = (" + good + " - " + bad + ") / (" + good +
						" + " + bad + ")");
				System.out.println("Sentiment score = " + sentiment);
				System.out.println("\n\n");
				System.out.println("Positivity score = " + good + "/(" + good + "+" + bad + ")");
				System.out.println("Positivity score = " + positivityScore + "%");
				System.out.println("\n\n\n********** \n\n\n\n");
 			}

 			else {
			System.out.println("\n\n\n**********\n\n\n");
			System.out.println("No positive or negative words found in input data.");
			System.out.println("\n\n\n**********\n\n\n");
 			}
 			return result;
		}
}
