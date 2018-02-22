package de.frosner.broccoli.models

import de.frosner.broccoli.RemoveSecrets
import play.api.libs.json._

import scala.util.Try

case class Instance(id: String, template: Template, parameterValues: Map[String, String]) extends Serializable {
  private def requireParameterValueConsistency(parameterValues: Map[String, String], template: Template) = {
    val realParametersWithValues = parameterValues.keySet ++ template.parameterInfos.flatMap {
      case (key, info) => info.default.map(Function.const(key))
    }
    require(
      template.parameters == realParametersWithValues,
      s"The given parameters values (${parameterValues.keySet}) " +
        s"need to match the ones in the template (${template.parameters})."
    )
  }

  requireParameterValueConsistency(parameterValues, template)
}

object Instance {
  implicit val instanceApiWrites: Writes[Instance] = {
    import Template.templateApiWrites
    Json.writes[Instance]
  }

  implicit val instancePersistenceWrites: Writes[Instance] = {
    import Template.templatePersistenceWrites
    Json.writes[Instance]
  }

  implicit val instancePersistenceReads: Reads[Instance] = {
    import Template.templatePersistenceReads
    Json.reads[Instance]
  }

  /**
    * Remove secrets from an instance.
    *
    * This instance removes all values of parameters marked as secrets from the instance parameters.
    */
  implicit val instanceRemoveSecrets: RemoveSecrets[Instance] = RemoveSecrets.instance { instance =>
    // FIXME "censoring" through setting the values null is ugly but using Option[String] gives me stupid Json errors
    val parameterInfos = instance.template.parameterInfos
    instance.copy(parameterValues = instance.parameterValues.map {
      case (parameter, value) =>
        val possiblyCensoredValue = if (parameterInfos.get(parameter).exists(_.secret.contains(true))) {
          null
        } else {
          value
        }
        (parameter, possiblyCensoredValue)
    })
  }
}
