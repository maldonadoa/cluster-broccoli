package de.frosner.broccoli.models

import com.hubspot.jinjava.Jinjava
import de.frosner.broccoli.models.ParameterInfo.parameterInfoWrites
import org.apache.commons.codec.digest.DigestUtils
import play.api.libs.functional.syntax._
import play.api.libs.json._

import scala.collection.JavaConversions._
import scala.collection.immutable.TreeSet

case class Template(id: String, template: String, description: String, parameterInfos: Map[String, ParameterInfo])
    extends Serializable {

  @transient
  lazy val parameters: Set[String] = {
    val jinjava = new Jinjava()
    val renderResult = jinjava.renderForResult(template, Map.empty[String, String])
    // TODO: get rid of this after the integration tests are in Scala https://travis-ci.org/FRosner/cluster-broccoli/jobs/297629476#L1771
    val uniqueVariables = TreeSet.empty[String] ++ renderResult.getContext.getResolvedValues.toSet
    uniqueVariables
  }

  @transient
  lazy val version: String = DigestUtils.md5Hex(template.trim() + "_" + parameterInfos.toString)

}

object Template {

  implicit val templateApiWrites: Writes[Template] = (
    (JsPath \ "id").write[String] and
      (JsPath \ "description").write[String] and
      (JsPath \ "parameters").write[Set[String]] and
      (JsPath \ "parameterInfos").write[Map[String, ParameterInfo]] and
      (JsPath \ "version").write[String]
  )((template: Template) =>
    (template.id, template.description, template.parameters, template.parameterInfos, template.version))

  implicit val templatePersistenceReads: Reads[Template] = Json.reads[Template]

  implicit val templatePersistenceWrites: Writes[Template] = Json.writes[Template]

}
