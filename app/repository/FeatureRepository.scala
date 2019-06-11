package repository

import anorm.SqlParser._
import anorm._
import javax.inject.Inject
import models.Feature.backgroundFormat
import models._
import play.api.Logging
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.libs.json.Json
import slick.jdbc.JdbcProfile
import utils._

import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration.Duration.Inf

class FeatureRepository @Inject()(val dbConfigProvider: DatabaseConfigProvider, tagRepository: TagRepository, scenarioRepository: ScenarioRepository)
                                 (implicit ec: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] with Logging {

  import dbConfig.profile.api._

  class Features(tag: Tag) extends Table[Feature](tag, "feature") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def branchId= column[Long]("branchId")
    def path = column[String]("path")
    def backgroundAsJson = column[Option[String]]("backgroundAsJson")
    def language = column[Option[String]]("language")
    def keyword = column[String]("keyword")
    def name = column[String]("name")
    def description = column[String]("description")
    def comments = column[String]("comments")

    def apply(id: Long, branchId: Long, path: String, backgroundAsJson: Option[String], language: Option[String], keyword: String, name: String, description: String, comments: String): Feature = {
      Feature(id, branchId, path.fixPathSeparator, backgroundAsJson.map(Json.parse(_).as[Background]), tagRepository.findAllByFeatureId(id), language, keyword, name, description, scenarioRepository.findAllByFeatureId(id), comments.split("\n").filterNot(_.isEmpty))
    }

    def unapply(feature: Feature): Option[(Long, Long, String, Option[String], Option[String], String, String, String, String)] = {
      Some((feature.id, feature.branchId, feature.path, feature.background.map(Json.toJson(_).toString), feature.language, feature.keyword, feature.name, feature.description, feature.comments.mkString("\n")))
    }

    def * = (id, branchId, path, backgroundAsJson, language, keyword, name, description, comments) <> ((apply _).tupled, unapply)
  }
  lazy val features = TableQuery[Features]

  def findAll(): Seq[Feature] = {
    Await.result(db.run(features.result), Inf)
  }

  def findAllFeaturePaths(): Seq[FeaturePath] = {
    Await.result(db.run(features.result), Inf).map(feature => FeaturePath(feature.branchId, feature.path))
  }

  def existsById(id: Long): Boolean = {
    Await.result(db.run(features.filter(_.id === id).exists.result), Inf)
  }

  def save(feature: Feature): Option[Feature] = {
    Await.result(db.run((features returning features).insertOrUpdate(feature))/*.flattenOption.*/.logError(s"Error while saving feature ${feature.path}"), Inf).map { savedFeature =>
      scenarioRepository.deleteAllByFeatureId(savedFeature.id)
      scenarioRepository.saveAll(savedFeature.id, savedFeature.scenarios)

      tagRepository.deleteAllByFeatureId(savedFeature.id)
      tagRepository.saveAllByFeatureId(savedFeature.id, savedFeature.tags)

      savedFeature
    }
  }

  def findById(id: Long): Option[Feature] = {
    Await.result(db.run(features.filter(_.id === id).result.headOption), Inf)
  }

  def findByBranchIdAndPath(branchId: Long, path: String): Option[Feature] = {
    Await.result(db.run(features.filter(f => f.branchId === branchId && f.path === path).result.headOption), Inf)
  }

  def findAllByBranchId(branchId: Long): Seq[Feature] = {
    Await.result(db.run(features.filter(_.branchId === branchId).result), Inf)
  }

  def saveAll(features: Seq[Feature]): Seq[Option[Feature]] = {
    features.map(save)
  }

  def deleteById(id: Long): Unit = {
    scenarioRepository.deleteAllByFeatureId(id)
    tagRepository.deleteAllByFeatureId(id)
    Await.result(db.run(features.filter(_.id === id).delete), Inf)
  }

  def delete(feature: Feature): Unit = {
    deleteById(feature.id)
  }

  def deleteAll(): Unit = {
    scenarioRepository.deleteAll()
    tagRepository.deleteAll()
    Await.result(db.run(features.delete), Inf)
  }

  def deleteAllByBranchId(branchId: Long): Unit = {
    findAllByBranchId(branchId).foreach(delete)
  }

  def count(): Long = {
    Await.result(db.run(features.length.result), Inf)
  }
}
