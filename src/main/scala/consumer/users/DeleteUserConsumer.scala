package consumer.users

import consumer.KafkaConsumer
import dao.UsersDao
import io.circe.parser.decode
import scala.concurrent.ExecutionContext.Implicits.global
import io.circe.generic.auto._
import kafka.common.{OffsetAndMetadata, OffsetMetadata, TopicAndPartition}
import mappings.JsonMappings
import models.User
import utils.Config

import scala.util.Failure

object DeleteUserConsumer extends App with Config with JsonMappings {


  val consumer = new KafkaConsumer(kafkaDeleteUserTopic)

  while (true) {
    consumer.read() match {
      case Some(message) =>
        println("Getting message.......................  " + message.key())
        val topicAndPartition = TopicAndPartition(message.topic, message.partition)
        val offsetMetadata = OffsetMetadata(message.offset)
        val offsetAndMetadata = OffsetAndMetadata(offsetMetadata)
        val offsetMap :Map[TopicAndPartition, OffsetAndMetadata]= Map(topicAndPartition -> offsetAndMetadata)
        val key = new String(message.key())

        UsersDao.delete(key.toInt)
          .onComplete {
            case scala.util.Success(e) =>
              println (s"Delete User Consumer Success ${e}")
              if( e == 1)
                consumer.commitOffset(offsetMap)

            case Failure(e) =>
              println(s"Delete User Consumer Failure ${e}")
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
