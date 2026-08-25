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
    fun fromDB(selected: Boolean?, value: String?): Selection = when (selected) {
      null if value == null -> Selection.Unanswered
      false if value == null -> Selection.No
      true if !value.isNullOrBlank() -> Selection.Yes(value)
      else -> throw IllegalStateException("Invalid Selection state: selected=$selected, value=$value")
    }
  }
}

fun Selection.toTriState(): Boolean? = when (this) {
  is Selection.Yes -> true
  Selection.No -> false
  Selection.Unanswered -> null
}

fun Selection.value(): String? = if (this is Selection.Yes) this.value else null
