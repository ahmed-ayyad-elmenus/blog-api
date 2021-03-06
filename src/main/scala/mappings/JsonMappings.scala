package mappings

import models.{Comment, Post, User}
import spray.json.DefaultJsonProtocol

trait JsonMappings extends DefaultJsonProtocol {
  implicit val userFormat = jsonFormat5(User.apply)
  implicit val postFormat = jsonFormat4(Post.apply)
  implicit val commentFormat = jsonFormat4(Comment.apply)
}