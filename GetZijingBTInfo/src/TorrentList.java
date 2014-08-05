package zijing;

/**
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

public class TorrentList {
	
	public static class TorrentMapper extends Mapper<LongWritable, Text, Text, Text>{
		
		private Text user = new Text();
		
		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
			String[] str = value.toString().split("	");
			
			Text torrentId = new Text(str[0]);
			String userList = "";
			if(str.length > 1) {
				userList = str[1];
			}
			StringTokenizer itr = new StringTokenizer(userList, ",");
			
			while (itr.hasMoreTokens()) {
				user.set(itr.nextToken());
				context.write(user, torrentId);
			}
		}
	}
	
	public static class TorrentReducer extends Reducer<Text, Text, Text, Text> {
		
		private Text result = new Text();

		public void reduce(Text key, Iterable<Text> values,  Context context) throws IOException, InterruptedException {
			String torrentList = "";

			for (Text val:values) {
				if (torrentList.length() > 0) {
					torrentList += ",";
				}
				torrentList += val.toString();
			}
			result.set(torrentList);
			context.write(key, result);
		}
	}
	
	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();
		String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
		if (otherArgs.length != 2) {
			System.err.println("Usage: torrentlist <in> <out>");
			System.exit(2);
		}
		Job job = new Job(conf, "torrent list");
		job.setJarByClass(TorrentList.class);
		job.setMapperClass(TorrentMapper.class);
		job.setReducerClass(TorrentReducer.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
		FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
		System.exit(job.waitForCompletion(true) ? 0 : 1);
	}
}
