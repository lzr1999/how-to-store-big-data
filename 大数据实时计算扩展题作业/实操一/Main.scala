import org.apache.flink.api.common.functions.{FlatMapFunction, MapFunction}
import org.apache.flink.streaming.api.datastream.{DataStream, DataStreamSource}
import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.api.scala._

object Main {

  val target = "b"

  def main(args: Array[String]) {

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    //Linux or Mac:nc -l 9999
    //Windows:nc -l -p 9999
    val text = env.socketTextStream("localhost", 9999)
    val stream = text.flatMap {
      _.toLowerCase.split("\\W+") filter {
        _.contains(target)
      }
    }.map (
      new MapFunction[String,(String,Int)](){
        override def map(t:String):(String,Int)={
          val chars=t.toLowerCase.split("")
          val cnt:Int = chars.count(
            p=new Function[String,Boolean]{
              override def apply(v1:String):Boolean={
                if(v1.equals(target)){
                  return true
                }
                return false
              }
            })
          return (target,cnt)
        }
      }
    ).keyBy(0).timeWindow(Time.seconds(60)).sum(1)
    stream.print()
    env.execute("Window Stream WordCount")
    }



  }










