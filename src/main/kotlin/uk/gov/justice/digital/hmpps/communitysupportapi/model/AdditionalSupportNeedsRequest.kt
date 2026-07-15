package uk.gov.justice.digital.hmpps.communitysupportapi.model

import uk.gov.justice.digital.hmpps.communitysupportapi.entity.PersonAdditionalSupportNeeds

data class AdditionalSupportNeedsRequest(
  val physicalHealth: String? = null,
  val mentalEmotionalHealth: String? = null,
  val neurodiversity: String? = null,
  val locationTravel: String? = null,
  val caringResponsibilities: String? = null,
  val employmentResponsibilities: String? = null,
  val diversity: String? = null,
  val anythingElse: String? = null,
  val needsAdditionalSupport: Boolean = false,
) {
  companion object {
    fun from(supportNeeds: PersonAdditionalSupportNeeds): AdditionalSupportNeedsRequest = AdditionalSupportNeedsRequest(
      physicalHealth = supportNeeds.physicalHealthDetails,
      mentalEmotionalHealth = supportNeeds.neurodiversityDetails,
      neurodiversity = supportNeeds.neurodiversityDetails,
      locationTravel = supportNeeds.locationTravelDetails,
      caringResponsibilities = supportNeeds.caringResponsibilitiesDetails,
      employmentResponsibilities = supportNeeds.employmentResponsibilitiesDetails,
      diversity = supportNeeds.diversityDetails,
      anythingElse = supportNeeds.anythingElseDetails,
      needsAdditionalSupport = !supportNeeds.noAdditionalSupportNeeded,
    )
  }
}
