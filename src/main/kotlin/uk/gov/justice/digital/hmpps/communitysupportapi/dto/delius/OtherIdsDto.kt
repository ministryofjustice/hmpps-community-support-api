package uk.gov.justice.digital.hmpps.communitysupportapi.dto.delius

data class OtherIdsDto(
  val crn: String,
  val pncNumber: String? = null,
  val croNumber: String? = null,
  val niNumber: String? = null,
  val nomsNumber: String? = null,
  val immigrationNumber: String? = null,
  val mostRecentPrisonerNumber: String? = null,
  val previousCrn: String? = null,
)
