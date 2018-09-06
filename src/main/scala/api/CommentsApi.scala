package api

import dao.CommentsDao

import scala.concurrent.ExecutionContext.Implicits.global
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import mappings.JsonMappings
import models.{Comment, CommentId}
import akka.http.scaladsl.server.Directives._
import producer.CommentProducer
import spray.json._
import utils.Config

trait CommentsApi extends JsonMappings with Config{
  val commentsApi =
    (path("users"/IntNumber/"posts"/IntNumber/"comments") & get ) {(userId, postId) =>
       complete (CommentsDao.findAll(userId, postId).map(_.toJson))
    }~
      (path("users"/IntNumber/"posts"/IntNumber/"comments"/IntNumber) & get) { (userId, postId, commentId) =>
        complete (CommentsDao.findById(userId, postId, commentId).map(_.toJson))
    }~
      (path("comments") & post) { entity(as[Comment]) { comment =>
        val id = comment.id
        id match {
          case Some(e) =>
            try{
              val response = CommentsDao.findById(comment.userId, comment.postId, e)
                .map (_ => {
                  s" This Comment already exists"
                }).recover{case _ =>{
                CommentProducer.sendComment(e.toInt, comment, kafkaAddCommentTopic)
                s" We sent add signal successfully"
                }
              }
              complete(response)
            }catch{
              case ex: Exception =>
                ex.printStackTrace()
                complete(ex)
            }

          case None =>

            CommentProducer.sendComment(comment, kafkaAddCommentTopic)
            complete(s" We sent add signal successfully")
        }
      }
    }~
      (path("users"/IntNumber/"posts"/IntNumber/"comments"/IntNumber) & put) { (userId, postId, commentId) => entity(as[Comment]) { newcomment =>
        val response = CommentsDao.findById(userId, postId, commentId)
          .map (comment => {
            CommentProducer.sendComment(commentId, newcomment, kafkaUpdateCommentTopic)
            s"We sent update signal successfully"

          }).recover{case _ =>{
          s" This comment is not exists"
        }
        }
        complete(response)
      }
    }~
      (path("users"/IntNumber/"posts"/IntNumber/"comments"/IntNumber) & delete) { (userId, postId, commentId) =>
        val response = CommentsDao.findById(userId, postId, commentId)
          .map (comment => {
            CommentProducer.sendComment(commentId, comment, kafkaUpdateCommentTopic)
            s"We sent delete signal successfully"

          }).recover{case _ =>{
          s" This comment is not exists"
          }
        }
        complete(response)
    }
}
