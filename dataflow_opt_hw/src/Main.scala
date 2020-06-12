import java.util.{Collections, Comparator, Properties, UUID}
import java.util

import com.bingocloud.{ClientConfiguration, Protocol}
import com.bingocloud.auth.BasicAWSCredentials
import com.bingocloud.services.s3.AmazonS3Client
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.datastream.DataStream
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer010
import org.apache.kafka.clients.consumer.{ConsumerRecord, ConsumerRecords, KafkaConsumer}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.nlpcn.commons.lang.util.IOUtil
import org.apache.kafka.common.TopicPartition

import scala.util.matching.Regex
import scala.collection.mutable.ArrayBuffer


object Main {
  //s3参数
  val accessKey = "C43D4E1AC76D0CF9EE06"
  val secretKey = "WzJFNEM3NTIwMkZBQjk2MzA3NDdERjM4Mzc1MUZFRjgxQjU0NzAzMERd"
  val endpoint = "scuts3.depts.bingosoft.net:29999"
  val bucket = "liziran"
  //要读取的文件
  val key = "daas.txt"

  //kafka参数
  val topic = "dataflow_lzr_test"
  val bootstrapServers = "bigdata35.depts.bingosoft.net:29035,bigdata36.depts.bingosoft.net:29036,bigdata37.depts.bingosoft.net:29037"
  val keyPrefix = "upload/"
  //上传数据间隔 单位毫秒
  val period = 5000
  //输入的kafka主题名称

  def main(args: Array[String]): Unit = {
    val s3Content = readFile()
    produceToKafka(s3Content)
//   ?
  }

  /**
   * 从s3中读取文件内容
   *
   * @return s3的文件内容
   */
  def readFile(): String = {
    val credentials = new BasicAWSCredentials(accessKey, secretKey)
    val clientConfig = new ClientConfiguration()
    clientConfig.setProtocol(Protocol.HTTP)
    val amazonS3 = new AmazonS3Client(credentials, clientConfig)
    amazonS3.setEndpoint(endpoint)
    val s3Object = amazonS3.getObject(bucket, key)
    IOUtil.getContent(s3Object.getObjectContent, "UTF-8")
  }

  /**
   * 把数据写入到kafka中
   *
   * @param s3Content 要写入的内容
   */
  def produceToKafka(s3Content: String): Unit = {
    val props = new Properties
    props.put("bootstrap.servers", bootstrapServers)
    props.put("acks", "all")
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    val producer = new KafkaProducer[String, String](props)
    val dataArr = s3Content.split("\n")
    val list = new ArrayBuffer[String]
    for(s<-dataArr) {
      if (!s.trim.isEmpty) {
        list.append(s)
      }
    }
    list.sortWith((o1:String,o2:String)=>o1.substring(o1.lastIndexOf("destination"))
      .compareTo(o2.substring(o2.lastIndexOf("destination")))<0)
    for (s <- list) {
      if (!s.trim.isEmpty) {
        val record = new ProducerRecord[String, String](topic, "destination", s)
        println("开始生产数据：" + s)
        producer.send(record)
      }
    }
    producer.flush()
    producer.close()
  }
}

