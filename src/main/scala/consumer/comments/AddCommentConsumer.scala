package consumer.comments

import consumer.KafkaConsumer
import dao.CommentsDao
import io.circe.generic.auto._
import io.circe.parser.decode
import kafka.common.{OffsetAndMetadata, OffsetMetadata, TopicAndPartition}
import mappings.JsonMappings
import models.Comment
import utils.Config

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Failure

object AddCommentConsumer extends App with Config with JsonMappings {


  val consumer = new KafkaConsumer(kafkaAddCommentTopic)

  while (true) {
    consumer.read() match {
      case Some(message) =>
        println("Getting message.......................  " + message.key())
        val topicAndPartition = TopicAndPartition(message.topic, message.partition)
        val offsetMetadata = OffsetMetadata(message.offset)
        val offsetAndMetadata = OffsetAndMetadata(offsetMetadata)
        val offsetMap :Map[TopicAndPartition, OffsetAndMetadata]= Map(topicAndPartition -> offsetAndMetadata)
        val key = new String(message.key())
        val value = decode[Comment](new String(message.message()))

        value match {
          case Right(rightComment)=>
            CommentsDao.create(rightComment)
              .onComplete {
                case scala.util.Success(e) =>
                  println (s"Create Comment Consumer Success ${e}")
                  consumer.commitOffset(offsetMap)

                case Failure(e) =>
                  println(s"Create Comment Consumer Failure ${e}")
              }

          case Left(e)=>
            println(s"Create Comment Consumer Left ${e}")
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
