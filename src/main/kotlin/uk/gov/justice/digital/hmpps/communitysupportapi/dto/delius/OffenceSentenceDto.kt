package uk.gov.justice.digital.hmpps.communitysupportapi.dto.delius

import com.fasterxml.jackson.annotation.JsonInclude
import java.time.LocalDate

data class OffenceSentenceDto(
  val offence: String? = null,
  val offenceSubCategory: String? = null,
  val outcome: String? = null,
  @get:JsonInclude(JsonInclude.Include.NON_NULL)
  val sentenceEndDate: LocalDate? = null,
  @get:JsonInclude(JsonInclude.Include.NON_NULL)
  val expectedReleaseDate: LocalDate? = null,
  val hasLicenceConditionsOrZones: Boolean? = null,
  val licenceConditionsOrZonesDetails: String? = null,
)
