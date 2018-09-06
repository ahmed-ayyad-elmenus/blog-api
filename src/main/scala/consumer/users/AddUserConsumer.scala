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

object AddUserConsumer extends App with Config with JsonMappings {


  val consumer = new KafkaConsumer(kafkaAddUserTopic)

  while (true) {
    consumer.read() match {
      case Some(message) =>
        println("Getting message.......................  " + message.key())
        val topicAndPartition = TopicAndPartition(message.topic, message.partition)
        val offsetMetadata = OffsetMetadata(message.offset)
        val offsetAndMetadata = OffsetAndMetadata(offsetMetadata)
        val offsetMap :Map[TopicAndPartition, OffsetAndMetadata]= Map(topicAndPartition -> offsetAndMetadata)
        val key = new String(message.key())
        val value = decode[User](new String(message.message()))

        value match {
          case Right(rightUser)=>
            UsersDao.create(rightUser)
              .onComplete {
                case scala.util.Success(e) =>
                  println (s"Create User Consumer Success ${e}")
                  consumer.commitOffset(offsetMap)

                case Failure(e) =>
                  println(s"Create User Consumer Failure ${e}")
              }

          case Left(e)=>
            println(s"Create User Consumer Left ${e}")
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
