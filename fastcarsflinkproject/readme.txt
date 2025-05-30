1、新建Flink Java Maven项目；

2、配置pom.xml

3、创建两个POJO类，分别代表事件流数据类型和结果流数据类型

4、创建车速异常车辆检测程序 FastCarsDetect.java

5、打包：mvn clean package

6、部署（包括jar包和属性配置文件）

	打开终端上传jar包和属性文件到Linux：scp -r /Users/yuchen/Documents/IntelliJ\ IDEA/fastcarsflinkproject/target/fastcarsflinkproject-1.0-SNAPSHOT.jar root@192.168.211.131:/usr/local/flink/flink-1.13.2
7、按下面的步骤说明执行

数据源的目的：
   模拟车辆监测到的数据。这个数据会发送到Kafka的cars主题。
   编写的流程序，读取cars主题的数据，计算平均速度，判断是否超速。如果超速，写出到fastcars主题。

fastcars_fat.jar  -----》 Kafka cars Topic -----》我们的Flink流程序 ----> Kafka fastcars Topic  ------》kafka-console-consumer.sh

执行步骤：
（前提是已经在linux搭建好所需环境：jdk Hadoop scala spark flink hbase zookeeper kafak
一、启动服务
1、启动zookeeper
	$ ./bin/zookeeper-server-start.sh config/zookeeper.properties

2、启动Kafka
	$ ./bin/kafka-server-start.sh config/server.properties

3、创建两个topic：
	$ ./bin/kafka-topics.sh --list --zookeeper 192.168.211.131:2185
	$ ./bin/kafka-topics.sh --zookeeper 192.168.211.131:2185 --replication-factor 1 --partitions 1 --create --topic cars   :生产者
	$ ./bin/kafka-topics.sh --zookeeper 192.168.211.131:2185 --replication-factor 1 --partitions 1 --create --topic fastcars

4、先在一个新的终端窗口中，执行消费者脚本，来拉取fastcars topic数据：
	$ ./bin/kafka-console-consumer.sh --bootstrap-server 192.168.211.131:9092 --topic fastcars

	
5、再执行流计算程序
    先启动flink集群：
    $ cd ~/flink-1.13.2/
    $ ./bin/start-cluster.sh
    再执行作业：
    $ /usr/local/export/flink/flink-1.13.2/bin/flink run --class "com.yuchen.FastCarsDetect" fastcarsflinkproject-1.0-SNAPSHOT.jar

6、最后执行模拟数据源程序
	$ java -jar fastcars_fat.jar  192.168.211.131:9092 cars

7、回到消息者脚本执行窗口，查看超速数据
