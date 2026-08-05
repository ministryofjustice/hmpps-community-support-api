package uk.gov.justice.digital.hmpps.communitysupportapi.dto

data class RefereeNameDto(
  val firstName: String,
  val middleName: String? = null,
  val lastName: String,
)
