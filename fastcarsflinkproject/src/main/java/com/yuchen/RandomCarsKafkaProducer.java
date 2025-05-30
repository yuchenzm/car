package com.yuchen;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;
import java.util.Random;

/**
 * Created by yuchen on 2023-02-08.
 * 模拟车辆传感器数据源 - 写Kafka
 */
public class RandomCarsKafkaProducer {

    private static final String TOPIC_CARS = "cars";      // topic

    public static void main(String[] args) {
        Properties props = new Properties();
        props.put("bootstrap.servers", "192.168.211.131:9092");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        KafkaProducer<String, String> producer = new KafkaProducer<>(props);
//        int numRecsToProduce = -1;		// -1 = infinite
        int numRecsToProduce = 1000;		// 连续产生1000条数据

        produceRecord(producer,numRecsToProduce);
    }

    private static void produceRecord(KafkaProducer<String, String> producer, int recordNum){
        int interval = 1000;

        // 生产有限数据记录
        if(recordNum > 0){
            // 生成一条数据，发送一条数据
            producer.send(generateCarRecord(TOPIC_CARS));
            try {
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            produceRecord(producer, recordNum - 1);
        }
        // 生产无限数据记录
        else if(recordNum < 0){
            producer.send(generateCarRecord(TOPIC_CARS));
            try {
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            produceRecord(producer, -1);
        }
    }

    // 生成一条车辆监测信息的方法
    private static ProducerRecord<String, String> generateCarRecord(String topic){
        Random r = new Random();
        String carName = "car" +r.nextInt(10);
        int speed = r.nextInt(150);
        float acc = r.nextFloat() * 100;
        long ts = System.currentTimeMillis();

        String value = carName + "," + speed + "," + acc + "," + ts;
        System.out.println("Generate car record: " + ts);
        //System.out.println("==Writing==：" + value);
        float d = r.nextFloat() * 100;
        if (d < 2) {
            // 产生随机延迟
            System.out.println("抱歉! 有一些网络延迟！");
            try {
                Thread.sleep(Float.valueOf(d*100).longValue());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return new ProducerRecord<>(topic,"key", value);
    }
}
