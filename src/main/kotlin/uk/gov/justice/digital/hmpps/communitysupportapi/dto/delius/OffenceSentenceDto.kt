package uk.gov.justice.digital.hmpps.communitysupportapi.dto.delius

import java.time.LocalDate

data class OffenceSentenceDto(
  val offence: String? = null,
  val offenceSubCategory: String? = null,
  val outcome: String? = null,
  val sentenceEndDate: LocalDate? = null,
  val expectedReleaseDate: LocalDate? = null,
)
