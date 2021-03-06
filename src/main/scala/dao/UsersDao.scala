package dao

import models.{UserId, User}
import slick.driver.MySQLDriver.api._
import scala.concurrent.Future

object UsersDao extends BaseDao{
  def findAll: Future[Seq[User]] = usersTable.result
  def findById(userId: UserId): Future[User] = usersTable.filter(_.id === userId).result.head
  def findByUsername(username: String): Future[User] = usersTable.filter(_.username === username).result.head
  def findByIdAndUsername(userId: UserId,username: String): Future[User] = usersTable.filter(user => user.id === userId || user.username === username).result.head
  def create(user: User): Future[UserId] = usersTable returning usersTable.map(_.id) += user
  def update(newUser: User, userId: UserId): Future[Int] = usersTable.filter(_.id === userId)
    .map(user => (user.username, user.password, user.gender, user.age))
    .update((newUser.userName, newUser.password, newUser.gender, newUser.age))

  def delete(userId: UserId): Future[Int] = usersTable.filter(_.id === userId).delete

}
