package org.apache.storm;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;

import org.apache.storm.topology.IRichBolt;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Tuple;

public class HashtagCounterBolt implements IRichBolt {
	Map<String, Integer> counterMap;
	private OutputCollector collector;
	
	PrintWriter out;
	String outputFile;
	

	public HashtagCounterBolt(String outputFile) {
		this.outputFile = outputFile;
	}

	@Override
	public void prepare(Map conf, TopologyContext context, OutputCollector collector) {
		this.counterMap = new HashMap<String, Integer>();
		this.collector = collector;	
		File f = new File(outputFile);
		if(f.exists() && !f.isDirectory()) { 
		    f.delete();
		}
		try {
			FileWriter fw = new FileWriter(outputFile, true);
		    BufferedWriter bw = new BufferedWriter(fw);
		    this.out = new PrintWriter(bw);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void execute(Tuple tuple) {
		String key = tuple.getString(0);

		if (!counterMap.containsKey(key)) {
			counterMap.put(key, 1);
		} else {
			Integer c = counterMap.get(key) + 1;
			counterMap.put(key, c);
		}
		for (Map.Entry<String, Integer> entry : counterMap.entrySet()) {
			System.out.println("Result: " + entry.getKey() + " : " + entry.getValue());
			out.println("Result: " + entry.getKey() + " : " + entry.getValue());
		}
		out.println("---------------------------------------------");
		
		collector.ack(tuple);

	}

	@Override
	// Not called in cluster mode
	public void cleanup() {
		for (Map.Entry<String, Integer> entry : counterMap.entrySet()) {
			System.out.println("Result: " + entry.getKey() + " : " + entry.getValue());
		}
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("hashtag"));
	}

	@Override
	public Map<String, Object> getComponentConfiguration() {
		return null;
	}

}
