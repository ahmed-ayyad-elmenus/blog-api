package api

import dao.UsersDao

import scala.concurrent.ExecutionContext.Implicits.global
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import mappings.JsonMappings
import models.User
import akka.http.scaladsl.server.Directives._
import producer.UserProducer
import spray.json._
import utils.Config


trait UsersApi extends JsonMappings with Config{

  val usersApi =
    (path("users") & get ) {
       complete (UsersDao.findAll.map(_.toJson))
    }~
    (path("users"/IntNumber) & get) { id =>
        complete (UsersDao.findById(id).map(_.toJson))
    }~
    (path("users") & post) { entity(as[User]) { user => {
      val id = user.id
      id match {
        case Some(x) =>
          val response = UsersDao.findByIdAndUsername(x, user.userName)
            .map (fetchedUserFromDatabase => {
              s" This user already exists, Be sure you have entered a unique id and username"
            }).recover{case _ =>{
            UserProducer.sendUser(user.id.get.toInt, user, kafkaAddUserTopic)
            s" We sent add signal successfully"
           }
          }
          complete(response)
        case None =>
          val response = UsersDao.findByUsername(user.userName)
            .map (fetchedUserFromDatabase => {
              s" This user already exists, Be sure you have entered a unique username"
            }).recover{case _ =>{
            UserProducer.sendUser(user, kafkaAddUserTopic)
            s" We sent add signal successfully"
           }
          }
          complete(response)
        }

      }
    }
    }~
    (path("users"/IntNumber) & put) { id => entity(as[User]) { user =>
//        complete (UsersDao.update(user, id).map(_.toJson))
        val response = UsersDao.findById(id)
          .map (_ => {
            UserProducer.sendUser(id, user, kafkaUpdateUserTopic)
            s"We sent update signal successfully"

          }).recover{case _ =>{
          s" This user is not exists"
          }
        }
        complete(response)
      }
    }~
    (path("users"/IntNumber) & delete) { userId =>
      val response = UsersDao.findById(userId)
        .map (user => {
          UserProducer.sendUser(userId, user, kafkaDeleteUserTopic)
          s"We sent delete signal successfully"

          }).recover{case _ =>{
          s" This user id is not exists"
          }
        }
      complete(response)
    }
}
