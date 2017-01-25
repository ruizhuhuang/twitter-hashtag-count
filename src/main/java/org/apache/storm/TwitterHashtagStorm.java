package org.apache.storm;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.*;

import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;
import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.topology.TopologyBuilder;

public class TwitterHashtagStorm {
	public static void main(String[] args) throws Exception {
		String consumerKey = args[0];
		String consumerSecret = args[1];

		String accessToken = args[2];
		String accessTokenSecret = args[3];
		String outputFile = args[4];

		String[] arguments = args.clone();
		String[] keyWords = Arrays.copyOfRange(arguments, 5, arguments.length);

		File f = new File(outputFile);
		if (f.exists() && !f.isDirectory()) {
			f.delete();
		}

		Config config = new Config();
		config.setDebug(true);

		TopologyBuilder builder = new TopologyBuilder();
		builder.setSpout("twitter-spout",
				new TwitterSampleSpout(consumerKey, consumerSecret, accessToken, accessTokenSecret, keyWords));

		builder.setBolt("twitter-hashtag-reader-bolt", new HashtagReaderBolt()).shuffleGrouping("twitter-spout");

		builder.setBolt("twitter-hashtag-counter-bolt", new HashtagCounterBolt(outputFile))
				.fieldsGrouping("twitter-hashtag-reader-bolt", new Fields("hashtag"));

		// local submit
		// LocalCluster cluster = new LocalCluster();
		// cluster.submitTopology("TwitterHashtagStorm", config,
		// builder.createTopology());

		// cluster submit
		config.setNumWorkers(20);
		config.setMaxSpoutPending(5000);
		StormSubmitter.submitTopology("TwitterHashtagStorm", config, builder.createTopology());
		// Thread.sleep(100000);
		// cluster.shutdown();
	}
}