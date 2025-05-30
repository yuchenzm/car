package com.yuchen;

import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.SlidingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;
import org.apache.flink.streaming.connectors.kafka.KafkaSerializationSchema;
import org.apache.flink.util.Collector;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.Properties;
/**
 * @author yuchen
 * 计算车辆没5秒的平均速度，并过滤掉速度小于100km/h的车辆，每两秒计算一次。
 * 执行：./bin/flink run --class com.yuchen.FastCarsDetect --jar xxxx.jar  broker topic1 topic2
 */
public class FastCarsDetect {

	private static String broker = "192.168.211.131:9092";			// kafka broker server
	private static String topic_source = "cars";		// 源topic
	private static String topic_sink = "fastcars";			// sink topic
	private static String group_id = "group-test";					// 消费者组

	// 加载属性文件
	/*
	static{
		String propertiesFile = "../../../my.properties";
		try {
			ParameterTool parameters = ParameterTool.fromPropertiesFile(propertiesFile);
			broker = parameters.get("broker");
			topic_source = parameters.get("topic_source");
			topic_sink = parameters.get("topic_sink");
			group_id = parameters.get("group_id");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}*/

	public static void main(String[] args) throws Exception {

		// 设置流执行环境
		final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

		// 启用检查点
		env.enableCheckpointing(5*60*1000);

		// 指定Kafka 数据源
		KafkaSource<String> source = KafkaSource.<String>builder()
				.setBootstrapServers(broker)
				.setTopics(topic_source)
				.setGroupId(group_id)
				.setStartingOffsets(OffsetsInitializer.latest())
				.setValueOnlyDeserializer(new SimpleStringSchema()) //进行简单反序列化，表明是字符串
//				.setDeserializer(KafkaRecordDeserializationSchema.valueOnly(StringDeserializer.class))
				.build();

		// 指定Kafka Sink
		//创建一个Kafka生产者，用于将处理后的数据写回到Kafka。配置了事务超时时间，并确保使用EXACTLY_ONCE语义保证数据一致性。
		Properties properties = new Properties();
		properties.setProperty("bootstrap.servers", broker);
		// Kafka brokers 默认的最大事务超时(transaction.max.timeout.ms)为15 minutes
		// 当使用Semantic.EXACTLY_ONCE语义时，下面这个属性值不能超过15分钟(默认为1 hour)
		properties.setProperty("transaction.timeout.ms", String.valueOf(5 * 60 * 1000));
		FlinkKafkaProducer myProducer = new FlinkKafkaProducer<CarAvgEvent>(
				topic_sink,                                       // 目标topic
				new ObjSerializationSchema(topic_sink),           // 序列化schema
				properties,                                       // producer配置
				FlinkKafkaProducer.Semantic.EXACTLY_ONCE          // 容错性
    	);
		myProducer.setWriteTimestampToKafka(true);

		// 水印策略允许最大2秒的事件乱序，并从输入数据中提取时间戳。
		WatermarkStrategy<String> watermarkStrategy = WatermarkStrategy
				.<String>forBoundedOutOfOrderness(Duration.ofSeconds(2))
				.withTimestampAssigner(new SerializableTimestampAssigner<String>() {
					@Override
					public long extractTimestamp(String s, long l) {
						return Long.parseLong(s.split(",")[3]);
					}
				});

		env
				// 指定Kafka数据源
				.fromSource(source, watermarkStrategy, "from cars topic")
				// 字符转换为DataStream<SensorReading>事件流
				.map(new MapFunction<String, CarEvent>() {
					@Override
					public CarEvent map(String s) throws Exception {
						String[] fields = s.split(",");
						return new CarEvent(
								fields[0],
								Integer.parseInt(fields[1]),
								Double.parseDouble(fields[2]),
								Long.parseLong(fields[3])
						);
					}
				})
				// 转换为KeyedStream
				.keyBy(new KeySelector<CarEvent, String>() {
					@Override
					public String getKey(CarEvent carEvent) throws Exception {
						return carEvent.carId;
					}
				})
				// 大小为5s,滑动为2s 的滑动窗口
				.window(SlidingEventTimeWindows.of(Time.seconds(5),Time.seconds(2)))
				// 执行增量聚合（增量聚合函数）窗口聚合
				.aggregate(new AvgSpeedAggFun(),new AvgSpeedProcessFun())
				//过滤
				.filter(new FilterFunction<CarAvgEvent>() {
					@Override
					public boolean filter(CarAvgEvent carAvgEvent) throws Exception {
						return carAvgEvent.avgSpeed > 100.0;
					}
				})
				//.print();
				.addSink(myProducer);

		// 触发流程序执行
		env.execute("Flink Sensor Temperature Demo");
	}

	// 增量处理函数
	public static class AvgSpeedAggFun implements AggregateFunction<
				CarEvent, 					// input
                Tuple2<Double,Long>, 		// acc, <sum, count>
				Double> {					// output, avg

		// 创建初始ACC
		@Override
		public Tuple2<Double,Long> createAccumulator() {
			return new Tuple2<>(0.0,0L);
		}

		// 累加每个传感器（每个分区）的事件
		@Override
		public Tuple2<Double,Long> add(CarEvent carEvent, Tuple2<Double,Long> acc) {
			return new Tuple2<>(carEvent.speed + acc.f0, acc.f1 + 1);
		}

		// 分区合并
		@Override
		public Tuple2<Double,Long> merge(
				Tuple2<Double,Long> acc1,
				Tuple2<Double,Long> acc2) {
			return new Tuple2<>(acc1.f0 + acc2.f0, acc1.f1 + acc2.f1);
		}

		// 返回每个车辆的平均速度
		@Override
		public Double getResult(Tuple2<Double,Long> t2) {
			return t2.f0/t2.f1;
		}
	}

	// 窗口处理函数
	public static class AvgSpeedProcessFun extends ProcessWindowFunction<
                Double,             				// input type
				CarAvgEvent,  						// output type
                String,                          	// key type
                TimeWindow> {                   	// window type

		@Override
		public void process(
				String id,							// key
				Context context,
				Iterable<Double> events,
				Collector<CarAvgEvent> out) {
			double average = Math.round(events.iterator().next()*100) / 100.0;
			long start = context.window().getStart();
			long end = context.window().getEnd();
			System.out.println("Processing timestamp: " + System.currentTimeMillis());
			System.out.println("Window start: " + new Timestamp(start));
			System.out.println("Window end: " + new Timestamp(end));
			out.collect(new CarAvgEvent(
					id,
					average,
					new Timestamp(context.window().getStart()).toString(),
					new Timestamp(context.window().getEnd()).toString())
			);
		}
	}

	// 自定义的序列化模式
	public static class ObjSerializationSchema implements KafkaSerializationSchema<CarAvgEvent> {

		private String topic;
		private ObjectMapper mapper;

		public ObjSerializationSchema(String topic) {
			super();
			this.topic = topic;
		}

		@Override
		public ProducerRecord<byte[], byte[]> serialize(CarAvgEvent obj, Long timestamp) {
			byte[] b = null;
			if (mapper == null) {
				mapper = new ObjectMapper();
			}
			try {
				b = mapper.writeValueAsBytes(obj);
			} catch (JsonProcessingException e) {
				// TODO
			}
			return new ProducerRecord<>(topic, b);
		}
	}
}
