package repository

import javax.inject.Inject
import models._
import play.api.db.slick._
import slick.jdbc.JdbcProfile
import utils._

import scala.concurrent.duration.Duration._
import scala.concurrent.{Await, ExecutionContext}

class BranchRepository @Inject()(val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {

  import dbConfig.profile.api._

  class Branches(tag: Tag) extends Table[Branch](tag, "branch") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def isStable = column[Boolean]("isStable")
    def projectId = column[String]("projectId")

    def apply(id: Long, name: String, isStable: Boolean, projectId: String): Branch = Branch(id, name, isStable, projectId)
    def unapply(branch: Branch): Option[(Long, String, Boolean, String)] = Some((branch.id, branch.name, branch.isStable, branch.projectId))
    def * = (id, name, isStable, projectId) <> ((apply _).tupled, unapply)
  }

  lazy val branches = TableQuery[Branches]


  def findAll(): Seq[Branch] = {
    Await.result(db.run(branches.result), Inf)
  }

  def findAllByProjectId(projectId: String): Seq[Branch] = {
    Await.result(db.run(branches.filter(_.projectId === projectId).result), Inf)
  }

  def save(b: Branch): Branch = {
    Await.result(db.run((branches returning branches).insertOrUpdate(b)).flattenOption.logError(s"Error while saving branch ${b.name} of project ${b.projectId}"), Inf)
  }

  def saveAll(branches: Seq[Branch]): Seq[Branch] = {
    branches.map(save)
  }

  def count(): Int = {
    Await.result(db.run(branches.length.result), Inf)
  }

  def deleteById(id: Long): Int = {
    Await.result(db.run(branches.filter(_.id === id).delete), Inf)
  }

  def deleteAll(branches: Seq[Branch]): Int = {
    Await.result(db.run(this.branches.filter(_.id.inSet(branches.map(_.id))).delete), Inf)
  }

  def deleteAll(): Int = {
    Await.result(db.run(branches.delete), Inf)
  }

  def delete(branch: Branch): Unit = {
    deleteById(branch.id)
  }

  def findAllById(ids: Seq[Long]): Seq[Branch] = {
    Await.result(db.run(branches.filter(_.id.inSet(ids)).result), Inf)
  }

  def findById(id: Long): Option[Branch] = {
    Await.result(db.run(branches.filter(_.id === id).result.headOption), Inf)
  }

  def findByProjectIdAndName(projectId: String, name: String): Option[Branch] = {
    Await.result(db.run(branches.filter(b => b.projectId === projectId && b.name === name).result.headOption), Inf)
  }

  def existsById(id: Long): Boolean = {
    Await.result(db.run(branches.filter(_.id === id).exists.result), Inf)
  }
}
