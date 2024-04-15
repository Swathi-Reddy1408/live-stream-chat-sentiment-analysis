/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.example;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;

import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;
import org.apache.flink.streaming.util.serialization.KeyedSerializationSchema;
import com.google.gson.Gson;


import java.util.List;
import java.util.Properties;

public class App {


	public static void main(String[] args) throws Exception {
		StreamExecutionEnvironment env = StreamExecutionEnvironment
				.createLocalEnvironmentWithWebUI(new Configuration());

		Gson gson = new Gson();
		ParameterTool parameters = ParameterTool.fromArgs(args);
		String[] twitchChannels = parameters
				.getRequired("twitchChannels")
				.trim()
				.split(",");
		//DataStreamSource<TwitchMessage> twitchDataStream = env.addSource(new TwitchSource(twitchChannels));

		//SingleOutputStreamOperator<Tuple2<TwitchMessage, Tuple2<List<Integer>, List<String>>>> analyzedStream =
				//twitchDataStream.map(new AnalyzeSentiment());



		env
				.addSource(new TwitchSource(twitchChannels))
				.map(new AnalyzeSentiment())
				//.map(result -> result.toString())  // Convert result to String for Kafka sink
				//.map(result -> gson.toJson(result))

				.addSink(new FlinkKafkaProducer<>(
						"[::1]:9092",  // Kafka broker address
						"demo_testing2",  // Kafka topic to write to
						new SimpleStringSchema()));
		        

		/*env
				.addSource(new TwitchSource(twitchChannels))
				.map(new AnalyzeSentiment())
				.print();*/


		env.execute("Flitch");
		env.close();
	}


}
