package producer

import io.circe.Encoder
import io.circe.generic.auto._
import io.circe.syntax._
import models.{Comment, Model, Post, User}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import utils.Config

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object UserProducer extends Config {

  import java.util.Properties

  def sendUser(id: Int, model: User, topic: String) :Future[User] = {
    val props = new Properties()
    props.put("bootstrap.servers", kafkaBootstrapServers)
    props.put("key.serializer", kafkaKeySerializer)
    props.put("value.serializer", kafkaValueSerializer)
    props.put("group.id", kafkaGroupId)

    val producer = new KafkaProducer[String, String](props)

    val record = new ProducerRecord(topic, id.toString, model.asJson.noSpaces)
    producer.send(record)
    producer.close()
    Future(model)
  }

  def sendUser( model: User, topic: String) :Future[User] = {
    val props = new Properties()
    props.put("bootstrap.servers", kafkaBootstrapServers)
    props.put("key.serializer", kafkaKeySerializer)
    props.put("value.serializer", kafkaValueSerializer)
    props.put("group.id", kafkaGroupId)

    val producer = new KafkaProducer[String, String](props)

    val id = model.id
    id match {
      case Some(x) =>
        val record = new ProducerRecord(topic, id.toString, model.asJson.noSpaces)
        producer.send(record)
        producer.close()
        Future(model)
      case None =>
        val record = new ProducerRecord(topic, s"None", model.asJson.noSpaces)
        producer.send(record)
        producer.close()
        Future(model)
    }


  }

}
