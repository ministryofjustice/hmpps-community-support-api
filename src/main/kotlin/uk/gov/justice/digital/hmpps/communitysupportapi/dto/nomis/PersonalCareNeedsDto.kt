package uk.gov.justice.digital.hmpps.communitysupportapi.dto.nomis

import java.time.LocalDate

data class PersonalCareNeedsDto(
  val problemType: String? = null,
  val problemCode: String? = null,
  val problemStatus: String? = null,
  val problemDescription: String? = null,
  val commentText: String? = null,
  val startDate: LocalDate? = null,
  val endDate: LocalDate? = null,
)
