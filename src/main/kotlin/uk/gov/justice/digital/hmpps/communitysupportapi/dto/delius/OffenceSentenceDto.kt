package uk.gov.justice.digital.hmpps.communitysupportapi.dto.delius

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import java.time.LocalDate

data class OffenceSentenceDto(
  val offence: String? = null,
  val offenceSubCategory: String? = null,
  val outcome: String? = null,
  @JsonInclude(Include.NON_NULL)
  val sentenceEndDate: LocalDate? = null,
  @JsonInclude(Include.NON_NULL)
  val expectedReleaseDate: LocalDate? = null,
  val hasLicenceConditionsOrZones: Boolean? = null,
  val licenceConditionsOrZonesDetails: String? = null,
)
