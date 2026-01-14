package uk.gov.justice.digital.hmpps.communitysupportapi.dto.nomis

import java.time.LocalDate

data class AliasesDto(
  val title: String? = null,
  val firstName: String,
  val lastName: String,
  val dateOfBirth: LocalDate? = null,
  val gender: String = "Unknown",
  val ethnicity: String? = null,
  val raceCode: String? = null,
)
