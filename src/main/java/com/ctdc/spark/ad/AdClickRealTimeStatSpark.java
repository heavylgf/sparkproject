package com.ctdc.spark.ad;

import com.ctdc.conf.ConfigurationManager;
import com.ctdc.constant.Constants;
import kafka.serializer.StringDecoder;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaPairInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka.KafkaUtils;
import scala.Tuple2;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 广告点击流量实时统计spark作业
 *
 * @author Administrator
 */
public class AdClickRealTimeStatSpark {

    public static void main(String[] args) {
        // 构建Spark Streaming 上下文
        SparkConf conf = new SparkConf()
                .setMaster("local[2]")
                .setAppName("AdClickRealTimeStatSpark");
//        // 构建Spark Streaming上下文
//        SparkConf conf = new SparkConf()
//                .setMaster("local[2]")
//                .setAppName("AdClickRealTimeStatSpark");
////				.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer");
////				.set("spark.default.parallelism", "1000");
////				.set("spark.streaming.blockInterval", "50");
////				.set("spark.streaming.receiver.writeAheadLog.enable", "true");

        // spark streaming的上下文是构建JavaStreamingContext对象
        // 而不是像之前的JavaSparkContext、SQLContext/HiveContext
        // 传入的第一个参数，和之前的spark上下文一样，也是SparkConf对象；第二个参数则不太一样

        // 第二个参数是spark streaming类型作业比较有特色的一个参数
        // 实时处理batch的interval
        // spark streaming，每隔一小段时间，会去收集一次数据源（kafka）中的数据，做成一个batch
        // 每次都是处理一个batch中的数据

        // 通常来说，batch interval，就是指每隔多少时间收集一次数据源中的数据，然后进行处理
        // 一遍spark streaming的应用，都是设置数秒到数十秒（很少会超过1分钟）

        // 咱们这里项目中，就设置5秒钟的batch interval
        // 每隔5秒钟，咱们的spark streaming作业就会收集最近5秒内的数据源接收过来的数据
        JavaStreamingContext jssc = new JavaStreamingContext(
                conf, Durations.seconds(5));

//        jssc.checkpoint("hdfs://192.168.1.105:9000/streaming_checkpoint");

        // 正式开始进行代码的编写
        // 实现咱们需要的实时计算的业务逻辑和功能

        // 创建针对Kafka数据来源的输入DStream（离线流，代表了一个源源不断的数据来源，抽象）
        // 选用kafka direct api（很多好处，包括自己内部自适应调整每次接收数据量的特性，等等）

        // 构建kafka参数map
        // 主要放置的就是，你要连接的kafka集群的地址（broker集群的地址列表）
        Map<String, String> kafkaParams = new HashMap<String, String>();
        kafkaParams.put("metadata.broker.list",
                ConfigurationManager.getProperty(Constants.KAFKA_METADATA_BROKER_LIST));

        // 构建topic set   可以构建多个
        String kafkaTopics = ConfigurationManager.getProperty(Constants.KAFKA_TOPICS);
        String[] kafkaTopicsSplited = kafkaTopics.split(",");
        Set<String> topics = new HashSet<String>();
        for (String kafkaTopic : kafkaTopicsSplited) {
            topics.add(kafkaTopic);
        }

        // 基于kafka direct api模式，构建出了针对kafka集群中指定topic的输入DStream
        // 两个值，val1，val2；val1没有什么特殊的意义；val2中包含了kafka topic中的一条一条的实时日志数据
        JavaPairInputDStream<String, String> adRealTimeLogDStream = KafkaUtils.createDirectStream(
                jssc,
                String.class,
                String.class,
                StringDecoder.class,
                StringDecoder.class,
                kafkaParams,
                topics);

        // 一条一条的实时日志
        // timestamp province city userid adid
        // 某个时间点 某个省份 某个城市 某个用户 某个广告

        // 计算出每5个秒内的数据中，每天每个用户每个广告的点击量

        // 通过对原始实时日志的处理
        // 将日志的格式处理成<yyyyMMdd_userid_adid, 1L>格式
        JavaPairDStream<String, Integer> dailyUserAdClickDStream = adRealTimeLogDStream.mapToPair(
                new PairFunction<Tuple2<String,String>, String, Integer>() {

                @Override
                public Tuple2<String, Integer> call(Tuple2<String, String> tuple) throws Exception {
                    return null;
                }

            });


        // 针对处理后的日志格式，执行reduceByKey算子即可
        // （每个batch中）每天每个用户对每个广告的点击量


        // 到这里为止，获取到了什么数据呢？
        // dailyUserAdClickCountDStream DStream
        // 源源不断的，每个5s的batch中，当天每个用户对每支广告的点击次数
        // <yyyyMMdd_userid_adid, clickCount>






//        JavaPairInputDStream<String, String> adRealTimeLogDStream = KafkaUtils.createDirectStream(
//                jssc,
//                String.class,
//                String.class,
//                StringDecoder.class,
//                StringDecoder.class,
//                kafkaParams,
//                topics);
//
////		adRealTimeLogDStream.repartition(1000);
//
//        // 根据动态黑名单进行数据过滤
//        JavaPairDStream<String, String> filteredAdRealTimeLogDStream =
//                filterByBlacklist(adRealTimeLogDStream);
//

        // 构建完 spark streaming上下文之后，记得要进行上下文的启动、等待执行结束、关闭
        jssc.start();
        jssc.awaitTermination();
        jssc.close();


    }

}
