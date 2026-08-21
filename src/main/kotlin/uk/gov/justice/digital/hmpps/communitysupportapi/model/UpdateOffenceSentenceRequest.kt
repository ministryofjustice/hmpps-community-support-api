package uk.gov.justice.digital.hmpps.communitysupportapi.model

import jakarta.validation.ValidationException
import java.time.LocalDate

data class UpdateOffenceSentenceRequest(
  val offence: String? = null,
  val offenceSubCategory: String? = null,
  val outcome: String? = null,
  val sentenceEndDate: LocalDate? = null,
  val expectedReleaseDate: LocalDate? = null,
  val hasLicenceConditionsOrZones: Boolean? = null,
  val licenceConditionsOrZonesDetails: String? = null,
) {
  fun validateAndNormalise(): UpdateOffenceSentenceRequest {
    if (hasLicenceConditionsOrZones == true && licenceConditionsOrZonesDetails.isNullOrBlank()) {
      throw ValidationException("licenceConditionsOrZonesDetails is required when hasLicenceConditionsOrZones is true")
    }

    return copy(
      licenceConditionsOrZonesDetails = if (hasLicenceConditionsOrZones == true) {
        licenceConditionsOrZonesDetails?.trim()
      } else {
        null
      },
    )
  }
}
