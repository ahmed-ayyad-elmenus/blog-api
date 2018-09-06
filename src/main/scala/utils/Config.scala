package utils

import com.typesafe.config.ConfigFactory

trait Config {
  private val config = ConfigFactory.load()
  private val httpConfig = config.getConfig("http")
  private val databaseConfig = config.getConfig("database")
  private val kafkaConfig = config.getConfig("kafka")
  val httpInterface = httpConfig.getString("interface")
  val httpPort = httpConfig.getInt("port")

  val databaseUrl = databaseConfig.getString("url")
  val databaseUser = databaseConfig.getString("user")
  val databasePassword = databaseConfig.getString("password")

  val kafkaBootstrapServers = kafkaConfig.getString("bootstrapServers")
  val kafkaZookeeperServer = kafkaConfig.getString("zookeeperServer")
  val kafkaKeySerializer = kafkaConfig.getString("keySerializer")
  val kafkaValueSerializer = kafkaConfig.getString("valueSerializer")
  val kafkaKeyDeserializer = kafkaConfig.getString("keyDeserializer")
  val kafkaValueDeserializer = kafkaConfig.getString("valueDeserializer")
  val kafkaGroupId = kafkaConfig.getString("groupId")
  val kafkaAddUserTopic = kafkaConfig.getString("addUserTopic")
  val kafkaUpdateUserTopic = kafkaConfig.getString("updateUserTopic")
  val kafkaDeleteUserTopic = kafkaConfig.getString("deleteUserTopic")
  val kafkaAddPostTopic = kafkaConfig.getString("addPostTopic")
  val kafkaUpdatePostTopic = kafkaConfig.getString("updatePostTopic")
  val kafkaDeletePostTopic = kafkaConfig.getString("deletePostTopic")
  val kafkaAddCommentTopic = kafkaConfig.getString("addCommentTopic")
  val kafkaUpdateCommentTopic = kafkaConfig.getString("updateCommentTopic")
  val kafkaDeleteCommentTopic = kafkaConfig.getString("deleteCommentTopic")


}
