package api

import dao.{PostsDao, UsersDao}

import scala.concurrent.ExecutionContext.Implicits.global
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import mappings.JsonMappings
import models.Post
import akka.http.scaladsl.server.Directives._
import producer.{PostProducer, UserProducer}
import spray.json._
import utils.Config

trait PostsApi extends JsonMappings with Config{
  val postsApi =
    (path("users"/IntNumber/"posts") & get){ userId =>
      complete (PostsDao.findUserPosts(userId).map(_.toJson))
    }~
    (path("users"/IntNumber/"posts"/IntNumber) & get) { (userId,postId) =>
      complete (PostsDao.findByUserIdAndId(userId, postId).map(_.toJson))
    }~
    (path("users"/IntNumber/"posts") & post) { userId => entity(as[Post]) { post =>
      val id = post.id
      id match {
        case Some(e) =>
          try{
            val response = PostsDao.findByUserIdAndId(userId, post.id.get)
              .map (_ => {
                s" This Post already exists"
              }).recover{case _ =>{
              PostProducer.sendPost(userId, post, kafkaAddPostTopic)
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

          PostProducer.sendPost(userId, post, kafkaAddPostTopic)
          complete(s" We sent add signal successfully")
      }

    }}~
    (path("users"/IntNumber/"posts"/IntNumber) & put) { (userId, id) => entity(as[Post]) { newpost =>
      val response = PostsDao.findByUserIdAndId(userId, id)
        .map (post => {
          PostProducer.sendPost(id, newpost, kafkaUpdatePostTopic)
          s"We sent update signal successfully"

        }).recover{case _ =>{
        s" This post is not exists"
        }
      }
      complete(response)
    }}~
    (path("users"/IntNumber/"posts"/IntNumber) & delete) { (userId, postId) =>
      val response = PostsDao.findByUserIdAndId(userId, postId)
        .map (post => {
          PostProducer.sendPost(postId, post, kafkaDeletePostTopic)
          s"We sent delete signal successfully"

        }).recover{case _ =>{
        s" This post id is not exists"
       }
      }
      complete(response)
    }
}
