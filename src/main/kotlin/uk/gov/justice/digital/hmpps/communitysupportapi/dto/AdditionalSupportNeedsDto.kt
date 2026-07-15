package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import uk.gov.justice.digital.hmpps.communitysupportapi.entity.PersonAdditionalSupportNeeds
import java.util.UUID

data class AdditionalSupportNeedsDto(
  val id: UUID? = null,
  val referralId: UUID,
  val personId: UUID,
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
    fun from(supportNeeds: PersonAdditionalSupportNeeds): AdditionalSupportNeedsDto = AdditionalSupportNeedsDto(
      id = supportNeeds.id,
      referralId = supportNeeds.referralId,
      personId = supportNeeds.personId,
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
