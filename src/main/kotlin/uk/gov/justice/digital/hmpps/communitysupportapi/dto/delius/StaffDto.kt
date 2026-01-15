package uk.gov.justice.digital.hmpps.communitysupportapi.dto.delius

data class StaffDto(
  val code: String? = null,
  val forenames: String? = null,
  val surname: String? = null,
  val unallocated: Boolean = false,
)
