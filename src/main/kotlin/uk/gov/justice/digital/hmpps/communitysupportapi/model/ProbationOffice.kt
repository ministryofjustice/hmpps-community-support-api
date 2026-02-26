package uk.gov.justice.digital.hmpps.communitysupportapi.model

data class ProbationOffice(
  val probationOfficeId: Int,
  val name: String,
  val address: String,
  val probationRegionId: String,
  val govUkUrl: String? = null,
  val deliusCRSLocationId: String? = null,
)
