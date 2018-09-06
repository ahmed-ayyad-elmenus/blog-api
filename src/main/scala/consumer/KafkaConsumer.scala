
package consumer

import java.util.Properties

import kafka.common.{OffsetAndMetadata, TopicAndPartition}
import kafka.consumer.{Consumer, ConsumerConfig, ConsumerTimeoutException, Whitelist}
import kafka.message.MessageAndMetadata
import kafka.serializer.DefaultDecoder
import utils.Config


class KafkaConsumer(topic: String) extends Config {



  private val props = new Properties()

  props.put("group.id", kafkaGroupId)
  props.put("zookeeper.connect", kafkaZookeeperServer)
  props.put("auto.offset.reset", "smallest")
  //2 minute consumer timeout
  props.put("consumer.timeout.ms", "120000")
  //commit after each 10 second
//  props.put("auto.commit.interval.ms", "10000")
  private val config = new ConsumerConfig(props)
  private val connector = Consumer.create(config)
  private val filterSpec = new Whitelist(topic)
  private val streams = connector.createMessageStreamsByFilter(filterSpec, 1, new DefaultDecoder(), new DefaultDecoder())(0)

  lazy val iterator = streams.iterator()

  def read(): Option[MessageAndMetadata[Array[Byte], Array[Byte]]] =
    try {
      if (hasNext) {
        println("Getting message from queue.............")

        val message = iterator.next()
        Some(message)
      } else {
        None
      }
    } catch {
      case ex: Exception =>
        ex.printStackTrace()
        None
    }

   def commitOffset(offset: Map[TopicAndPartition, OffsetAndMetadata]) =
    try{
      connector.commitOffsets(offset, true)
    }
    catch {
      case timeOutEx: ConsumerTimeoutException =>
        false
      case ex: Exception =>
        ex.printStackTrace()
        println("Getting error when commit offsets ")
        false
    }

  private def hasNext(): Boolean =
    try
      iterator.hasNext()
    catch {
      case timeOutEx: ConsumerTimeoutException =>
        false
      case ex: Exception =>
        ex.printStackTrace()
        println("Getting error when reading message ")
        false
    }

  def close(): Unit = connector.shutdown()

}