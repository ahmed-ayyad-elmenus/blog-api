package consumer.comments

import consumer.KafkaConsumer
import dao.CommentsDao
import kafka.common.{OffsetAndMetadata, OffsetMetadata, TopicAndPartition}
import mappings.JsonMappings
import utils.Config

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Failure

object DeleteCommentConsumer extends App with Config with JsonMappings {


  val consumer = new KafkaConsumer(kafkaDeleteCommentTopic)

  while (true) {
    consumer.read() match {
      case Some(message) =>
        println("Getting message.......................  " + message.key())
        val topicAndPartition = TopicAndPartition(message.topic, message.partition)
        val offsetMetadata = OffsetMetadata(message.offset)
        val offsetAndMetadata = OffsetAndMetadata(offsetMetadata)
        val offsetMap :Map[TopicAndPartition, OffsetAndMetadata]= Map(topicAndPartition -> offsetAndMetadata)
        val key = new String(message.key())

        CommentsDao.delete(key.toInt)
          .onComplete {
            case scala.util.Success(e) =>
              println (s"Delete Comment Consumer Success ${e}")
              if( e == 1)
                consumer.commitOffset(offsetMap)

            case Failure(e) =>
              println(s"Delete Comment Consumer Failure ${e}")
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
