package consumer.posts

import consumer.KafkaConsumer
import dao.PostsDao
import io.circe.generic.auto._
import io.circe.parser.decode
import kafka.common.{OffsetAndMetadata, OffsetMetadata, TopicAndPartition}
import mappings.JsonMappings
import models.Post
import utils.Config

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Failure

object AddPostConsumer extends App with Config with JsonMappings {


  val consumer = new KafkaConsumer(kafkaAddPostTopic)

  while (true) {
    consumer.read() match {
      case Some(message) =>
        println("Getting message.......................  " + message.key())
        val topicAndPartition = TopicAndPartition(message.topic, message.partition)
        val offsetMetadata = OffsetMetadata(message.offset)
        val offsetAndMetadata = OffsetAndMetadata(offsetMetadata)
        val offsetMap :Map[TopicAndPartition, OffsetAndMetadata]= Map(topicAndPartition -> offsetAndMetadata)
        val key = new String(message.key())
        val value = decode[Post](new String(message.message()))

        value match {
          case Right(rightPost)=>
            PostsDao.create(rightPost)
              .onComplete {
                case scala.util.Success(e) =>
                  println (s"Create Post Consumer Success ${e}")
                  consumer.commitOffset(offsetMap)

                case Failure(e) =>
                  println(s"Create Post Consumer Failure ${e}")
              }

          case Left(e)=>
            println(s"Create Post Consumer Left ${e}")
        }
        // wait for 100 milli second for another read
        Thread.sleep(100)
      case None =>
        println("Queue is empty.......................  ")
        // wait for 2 second
        Thread.sleep(2 * 1000)
    }

  }

}
