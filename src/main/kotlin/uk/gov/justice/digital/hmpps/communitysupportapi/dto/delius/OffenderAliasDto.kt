package uk.gov.justice.digital.hmpps.communitysupportapi.dto.delius

import java.time.LocalDate

data class OffenderAliasDto(
  val id: String? = null,
  val dateOfBirth: LocalDate? = null,
  val firstName: String? = null,
  val middleNames: List<String> = emptyList(),
  val surname: String? = null,
  val gender: String = "Unknown",
)
