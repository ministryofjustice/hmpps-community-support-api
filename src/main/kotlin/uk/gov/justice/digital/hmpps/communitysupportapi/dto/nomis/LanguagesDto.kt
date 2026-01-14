package uk.gov.justice.digital.hmpps.communitysupportapi.dto.nomis

data class LanguagesDto(
  val type: String? = null,
  val code: String? = null,
  val readSkill: String? = null,
  val writeSkill: String? = null,
  val speakSkill: String? = null,
  val interpreterRequested: Boolean? = false,
)
