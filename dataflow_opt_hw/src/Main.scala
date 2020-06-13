import java.util.{Collections, Comparator, Properties, UUID}
import com.bingocloud.{ClientConfiguration, Protocol}
import com.bingocloud.auth.BasicAWSCredentials
import com.bingocloud.services.s3.AmazonS3Client
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer010
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.nlpcn.commons.lang.util.IOUtil
import scala.collection.mutable.ArrayBuffer
import scala.io.StdIn


object Main {
  //s3参数
  var accessKey = "C43D4E1AC76D0CF9EE06"
  var secretKey = "WzJFNEM3NTIwMkZBQjk2MzA3NDdERjM4Mzc1MUZFRjgxQjU0NzAzMERd"
  var endpoint = "scuts3.depts.bingosoft.net:29999"
  var bucket = "liziran"
  //要读取的文件
  var key = "daas.txt"

  //kafka参数
  val topic = "dataflow_lzr_test2"
  val bootstrapServers = "bigdata35.depts.bingosoft.net:29035,bigdata36.depts.bingosoft.net:29036,bigdata37.depts.bingosoft.net:29037"
  val keyPrefix = "upload/"
  //上传数据间隔 单位毫秒
  val period = 5000
  //输入的kafka主题名称

  def main(args: Array[String]): Unit = {

    accessKey = StdIn.readLine("请输入accessKey:")
    secretKey = StdIn.readLine("请输入secretKey:")
    endpoint = StdIn.readLine("请输入endpoint:")
    bucket = StdIn.readLine("请输入bucketName:")
    key = StdIn.readLine("请输入key:")
    val s3Content = readFile()
    produceToKafka(s3Content)
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)
    val kafkaProperties = new Properties()
    kafkaProperties.put("bootstrap.servers", bootstrapServers)
    kafkaProperties.put("group.id", UUID.randomUUID().toString)
    kafkaProperties.put("auto.offset.reset", "earliest")
    kafkaProperties.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    kafkaProperties.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    val kafkaConsumer = new FlinkKafkaConsumer010[String](topic,
      new SimpleStringSchema, kafkaProperties)
    kafkaConsumer.setCommitOffsetsOnCheckpoints(true)
    val inputKafkaStream = env.addSource(kafkaConsumer)
    inputKafkaStream.writeUsingOutputFormat(new S3Writer(accessKey, secretKey, endpoint, bucket, keyPrefix, period))
    env.execute()
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
    val b=list.sortWith{(o1:String,o2:String)=>
    {o1.substring(o1.lastIndexOf("destination"))
      .compareTo(o2.substring(o2.lastIndexOf("destination")))<0}}
    for (i <- 0 until b.length)
      println(b(i))
    for (i <- 0 until b.length) {
        val record = new ProducerRecord[String, String](topic, "destination", b(i))
        println("开始生产数据：" + b(i))
        producer.send(record)
    }
    producer.flush()
    producer.close()
 }
}

