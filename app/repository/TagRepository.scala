package repository

import javax.inject.Inject
import play.api.db.slick._
import slick.jdbc.JdbcProfile
import utils._

import scala.concurrent._
import scala.concurrent.duration.Duration.Inf

class TagRepository @Inject()(val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {

  import dbConfig.profile.api._

  case class ScenarioTag(scenarioId: Long, name: String)

  case class FeatureTag(featureId: Long, name: String)

  class Tags(tag: Tag) extends Table[String](tag, "tag") {
    def name = column[String]("name", O.PrimaryKey)
    def * = name
  }

  lazy val tags = TableQuery[Tags]

  class ScenarioTags(tag: Tag) extends Table[ScenarioTag](tag, "scenario_tag") {
    def scenarioId = column[Long]("scenarioId")
    def name = column[String]("name")

    def * = (scenarioId, name).mapTo[ScenarioTag]
    def pk = primaryKey("pk_scenario_tag", (scenarioId, name))
  }

  lazy val scenarioTags = TableQuery[ScenarioTags]

  class FeatureTags(tag: Tag) extends Table[FeatureTag](tag, "feature_tag") {
    def featureId = column[Long]("featureId")
    def name = column[String]("name")

    def * = (featureId, name).mapTo[FeatureTag]
    def pk = primaryKey("pk_scenario_tag", (featureId, name))
  }

  lazy val featureTags = TableQuery[FeatureTags]

  def count(): Long = {
    Await.result(db.run(tags.length.result), Inf)
  }

  private def deleteIfEmpty(tag: String) = {
    val featureTagCount = Await.result(db.run(featureTags.length.result), Inf)
    val scenarioTagCount = Await.result(db.run(scenarioTags.length.result), Inf)

    if (featureTagCount == 0 && scenarioTagCount == 0) {
      Await.result(db.run(tags.filter(_.name === tag).delete), Inf)
    }
  }

  def deleteByFeatureId(featureId: Long, tags: Seq[String]): Unit = {
    tags.foreach { tag =>
      Await.result(db.run(featureTags.filter(t => t.featureId === featureId && t.name === tag).delete), Inf)
      deleteIfEmpty(tag)
    }
  }

  def deleteAllByFeatureId(featureId: Long): Unit = deleteByFeatureId(featureId, findAllByFeatureId(featureId))

  def deleteAllByScenarioId(scenarioId: Long): Unit = deleteByScenarioId(scenarioId, findAllByScenarioId(scenarioId))

  def deleteByScenarioId(scenarioId: Long, tags: Seq[String]): Unit = {
    tags.foreach { tag =>
      Await.result(db.run(scenarioTags.filter(t => t.scenarioId === scenarioId && t.name === tag).delete), Inf)
      deleteIfEmpty(tag)
    }
  }

  def deleteAll(): Unit = {
    Await.result(db.run(tags.delete), Inf)
    Await.result(db.run(featureTags.delete), Inf)
    Await.result(db.run(scenarioTags.delete), Inf)
  }

  def deleteAllFeatureTag(): Unit = {
    val tags = findAll()
    Await.result(db.run(featureTags.delete), Inf)
    tags.foreach(deleteIfEmpty)
  }

  def deleteAllScenarioTag(): Unit = {
    val tags = findAll()
    Await.result(db.run(scenarioTags.delete), Inf)
    tags.foreach(deleteIfEmpty)
  }

  def findAll(): Seq[String] = {
    Await.result(db.run(tags.result), Inf)
  }

  def findAllByFeatureId(featureId: Long): Seq[String] = {
    Await.result(db.run(featureTags.filter(t => t.featureId === featureId).map(_.name).result), Inf)
  }

  def findAllByScenarioId(scenarioId: Long): Seq[String] = {
    Await.result(db.run(scenarioTags.filter(t => t.scenarioId === scenarioId).map(_.name).result), Inf)
  }

  def saveAllByFeatureId(featureId: Long, tags: Seq[String]): Seq[String] = {
    deleteAllByFeatureId(featureId)

    tags.foreach { tag =>
      Await.result(db.run(this.tags.insertOrUpdate(tag)).logError(s"Error while saving feature $featureId tag $tag"), Inf)
      Await.result(db.run(featureTags.insertOrUpdate(FeatureTag(featureId, tag))).logError(s"Error while saving feature $featureId tag $tag"), Inf)
    }

    findAllByFeatureId(featureId)
  }


  def saveAllByScenarioId(scenarioId: Long, tags: Seq[String]): Seq[String] = {
    deleteAllByScenarioId(scenarioId)

    tags.foreach { tag =>
      Await.result(db.run(this.tags.insertOrUpdate(tag)).logError(s"Error while saving scenario $scenarioId tag $tag"), Inf)
      Await.result(db.run(scenarioTags.insertOrUpdate(ScenarioTag(scenarioId, tag))).logError(s"Error while saving scenario $scenarioId tag $tag"), Inf)
    }

    findAllByScenarioId(scenarioId)
  }
}
