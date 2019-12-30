package com.cloudskys;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.util.Collector;

public class SocketWindowWordCount {

	public static void main(String[] args) throws Exception {

		// 创建 execution environment
		StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

		// 通过连接 socket 获取输入数据，这里连接到本地9000端口，如果9000端口已被占用，请换一个端口
		//这创建了一个字符串类型的 DataStream。DataStream 是 Flink 中做流处理的核心 API，上面定义了非常多常见的操作（如，过滤、转换、聚合、窗口、关联等）。
		//在本示例中，我们感兴趣的是每个单词在特定时间窗口中出现的次数，比如说5秒窗口。为此，我们首先要将字符串数据解析成单词和次数（使用Tuple2<String, Integer>表示），
		//第一个字段是单词，第二个字段是次数，次数初始值都设置成了1。我们实现了一个 flatmap 来做解析的工作，因为一行数据中可能有多个单词。
		
		//接着我们将数据流按照单词字段（即0号索引字段）做分组，这里可以简单地使用 keyBy(int index) 方法，得到一个以单词为 key 的Tuple2<String, Integer>数据流。然后我们可以在流上指定想要的窗口，并根据窗口中的数据计算结果。在我们的例子中，我们想要每5秒聚合一次单词数，每个窗口都是从零开始统计的：。
		//第二个调用的 .timeWindow() 指定我们想要5秒的翻滚窗口（Tumble）。第三个调用为每个key每个窗口指定了sum聚合函数，在我们的例子中是按照次数字段（即1号索引字段）相加。得到的结果数据流，将每5秒输出一次这5秒内每个单词出现的次数。

        //最后一件事就是将数据流打印到控制台，并开始执行：
		//最后的 env.execute 调用是启动实际Flink作业所必需的。所有算子操作（例如创建源、聚合、打印）只是构建了内部算子操作的图形。只有在execute()被调用时才会在提交到集群上或本地计算机上执行。
		DataStream<String> text = env.socketTextStream("localhost", 9000, "\n");

		// 解析数据，按 word 分组，开窗，聚合
		DataStream<Tuple2<String, Integer>> windowCounts = text
				.flatMap(new FlatMapFunction<String, Tuple2<String, Integer>>() {
					@Override
					public void flatMap(String value, Collector<Tuple2<String, Integer>> out) {
						for (String word : value.split("\\s")) {
							out.collect(Tuple2.of(word, 1));
						}
					}
				})
				.keyBy(0)
				.timeWindow(Time.seconds(5))
				.sum(1);

		// 将结果打印到控制台，注意这里使用的是单线程打印，而非多线程
		windowCounts.print().setParallelism(1);

		env.execute("Socket Window WordCount");
	}
}