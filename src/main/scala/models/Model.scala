package models

sealed trait Model

case class User(id: Option[UserId], userName: String, password: String, age: Int, gender: Int) extends Model
case class Post(id: Option[PostId], userId: UserId, title: String, content: String) extends Model
case class Comment(id: Option[CommentId], userId: UserId, postId: PostId, content: String) extends Model


