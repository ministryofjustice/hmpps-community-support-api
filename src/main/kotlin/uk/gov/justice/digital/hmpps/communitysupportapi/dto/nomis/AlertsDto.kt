package uk.gov.justice.digital.hmpps.communitysupportapi.dto.nomis

data class AlertsDto(
  val alertType: String? = null,
  val alertCode: String? = null,
  val active: Boolean = false,
  val expired: Boolean = false,
)
