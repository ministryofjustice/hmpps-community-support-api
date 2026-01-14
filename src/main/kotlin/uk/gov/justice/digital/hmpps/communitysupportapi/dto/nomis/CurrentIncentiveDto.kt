package uk.gov.justice.digital.hmpps.communitysupportapi.dto.nomis

import java.time.LocalDate

data class CurrentIncentiveDto(
  val level: LevelDto? = null,
  val dateTime: LocalDate? = null,
  val nextReviewDate: LocalDate? = null,
)

data class LevelDto(
  val code: String? = null,
  val description: String? = null,
)
