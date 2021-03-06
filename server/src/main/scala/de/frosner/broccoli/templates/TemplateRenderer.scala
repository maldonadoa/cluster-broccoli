package de.frosner.broccoli.templates

import javax.inject.Inject

import de.frosner.broccoli.instances.InstanceConfiguration
import de.frosner.broccoli.models.{Instance, ParameterInfo, ParameterType}
import org.apache.commons.lang3.StringEscapeUtils
import play.api.libs.json.{JsString, JsValue, Json}

/**
  * Renders json representation of the passed instance
  *
  * @param defaultType The default type of template parameters
  */
class TemplateRenderer(defaultType: ParameterType) {
  def sanitize(parameter: String, value: String, parameterInfos: Map[String, ParameterInfo]): String = {
    val parameterType = parameterInfos
      .get(parameter)
      .flatMap(_.`type`)
      .getOrElse(defaultType)
    val sanitized = parameterType match {
      case ParameterType.Raw => value
      case ParameterType.String =>
        StringEscapeUtils.escapeJson(value)
    }
    sanitized
  }

  def renderJson(instance: Instance): JsValue = {
    val template = instance.template
    val parameterInfos = template.parameterInfos
    val parameterValues = instance.parameterValues

    val templateWithValues = parameterValues.foldLeft(template.template) {
      case (intermediateTemplate, (parameter, value)) =>
        intermediateTemplate.replaceAllLiterally(s"{{$parameter}}", sanitize(parameter, value, parameterInfos))
    }
    val parameterDefaults = parameterInfos.flatMap {
      case (parameterId, parameterInfo) => parameterInfo.default.map(default => (parameterId, default))
    }
    val templateWithDefaults = parameterDefaults.foldLeft(templateWithValues) {
      case (intermediateTemplate, (parameter, value)) =>
        intermediateTemplate.replaceAllLiterally(s"{{$parameter}}", sanitize(parameter, value, parameterInfos))
    }
    Json.parse(templateWithDefaults)
  }
}
