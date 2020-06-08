import java.util.Properties

import com.bingocloud.{ClientConfiguration, Protocol}
import com.bingocloud.auth.BasicAWSCredentials
import com.bingocloud.services.s3.AmazonS3Client
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.nlpcn.commons.lang.util.IOUtil
import java.sql.{Connection, DriverManager}

object Main {
  // mysql参数
  val username = "user31"
  val password = "pass@bingo31"
  val driver = "com.mysql.jdbc.Driver"
  val url = "jdbc:mysql://bigdata28.depts.bingosoft.net:23307/user31_db"
  var connection: Connection = null


  //kafka参数
  val topic = "mysql_content"
  val bootstrapServers = "bigdata35.depts.bingosoft.net:29035,bigdata36.depts.bingosoft.net:29036,bigdata37.depts.bingosoft.net:29037"

  def main(args: Array[String]): Unit = {
    val mysqlContent = readMySQL()
    produceToKafka(mysqlContent)
  }


  def readMySQL(): String = {
    Class.forName("com.mysql.jdbc.Driver")
    connection = DriverManager.getConnection(url, username, password)
    val stateMent = connection.createStatement()
    val resultSet = stateMent.executeQuery("select * from mysql_exp7")
    val metaData = resultSet.getMetaData
    val columnCount = metaData.getColumnCount()
    val builder = new StringBuilder()
    while(resultSet.next()){
      var temp = ""
      var index = 1
      while(index <= columnCount) {
        temp += resultSet.getString(index) + "\t"
        index += 1
      }
      builder.append(temp+"\n")
    }
    return builder.toString()
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
    for (s <- dataArr) {
      if (!s.trim.isEmpty) {
        val record = new ProducerRecord[String, String](topic, null, s)
        println("开始生产数据：" + s)
        producer.send(record)
      }
    }
    producer.flush()
    producer.close()
  }
}
