package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME,
  include = JsonTypeInfo.As.PROPERTY,
  property = "selected",
  visible = true,
)
@JsonSubTypes(
  JsonSubTypes.Type(value = Selection.Yes::class, name = "Yes"),
  JsonSubTypes.Type(value = Selection.No::class, name = "No"),
  JsonSubTypes.Type(value = Selection.Unanswered::class, name = "Unanswered"),
)
sealed interface Selection {
  data class Yes(val value: String) : Selection
  data object No : Selection
  data object Unanswered : Selection
  companion object {
    fun fromString(value: String?): Selection = if (value == null) No else Yes(value)
  }
}