import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.streaming.{Duration, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import org.apache.spark.streaming.kafka010.KafkaUtils
import org.neo4j.driver.{AuthTokens, Config, GraphDatabase}

import scala.util.matching.Regex

object Hashtags {
  def main(args: Array[String]){
    val conf = new SparkConf()
    conf.setMaster("local")
    conf.setAppName("Hashtag Streaming")

    val sc = new SparkContext(conf)
    val streamingContext = new StreamingContext(sc, Duration(2000))

    val kafkaParams = Map[String, Object](
      "bootstrap.servers" -> "localhost:9092",
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "group.id" -> "twitter-news",
      "auto.offset.reset" -> "latest",
      "enable.auto.commit" -> (false: java.lang.Boolean)
    )

    val topics = Array("twitter-news-topic")
    val stream = KafkaUtils.createDirectStream[String, String](
      streamingContext,
      PreferConsistent,
      Subscribe[String, String](topics, kafkaParams)
    )

    val user = new Regex(raw"(@\w+)")
    val hashtags = new Regex(raw"(#\w+)")

    stream.foreachRDD { recordRdd =>
      recordRdd.foreach { record =>
        val allUsers = for (m <- user.findAllMatchIn(record.value)) yield m.group(1)
        val allHashtags = for (m <- hashtags.findAllMatchIn(record.value)) yield m.group(1)

        if (allUsers.nonEmpty && allHashtags.nonEmpty) {
          val driver = GraphDatabase.driver("bolt://localhost:7687",
            AuthTokens.basic("neo4j", "neo4j"))

          allUsers.foreach { u =>
            allHashtags.foreach { h =>
              val session = driver.session()
              session.run(
                s"MERGE (_u: User {name: '$u'})\n" +
                s"MERGE (_h: Hashtag {name: '$h'})")

              session.run(
                  s"MATCH (c: User {name: '$u'})\n" +
                  s"MATCH (h: Hashtag {name: '$h'})\n" +
                  s"MERGE (c)-[:to]->(h)")

              session.close()
            }
          }
        }
      }
    }

    streamingContext.start()
    streamingContext.awaitTermination()
  }
}
