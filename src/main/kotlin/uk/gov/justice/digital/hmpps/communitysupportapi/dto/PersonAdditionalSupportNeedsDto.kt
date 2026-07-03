package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import uk.gov.justice.digital.hmpps.communitysupportapi.entity.PersonAdditionalSupportNeeds
import java.util.UUID

data class PersonAdditionalSupportNeedsDto(
  val id: UUID? = null,
  val referralId: UUID,
  val personId: UUID,
  val physicalHealthDetails: String? = null,
  val mentalEmotionalHealthDetails: String? = null,
  val neurodiversityDetails: String? = null,
  val locationTravelDetails: String? = null,
  val caringResponsibilitiesDetails: String? = null,
  val employmentResponsibilitiesDetails: String? = null,
  val diversityDetails: String? = null,
  val anythingElseDetails: String? = null,
  val noAdditionalSupportNeeded: Boolean = false,
  val interpreterLanguage: String? = null,
) {
  companion object {
    fun from(supportNeeds: PersonAdditionalSupportNeeds): PersonAdditionalSupportNeedsDto = PersonAdditionalSupportNeedsDto(
      id = supportNeeds.id,
      referralId = supportNeeds.referralId,
      personId = supportNeeds.personId,
      physicalHealthDetails = supportNeeds.physicalHealthDetails,
      mentalEmotionalHealthDetails = supportNeeds.neurodiversityDetails,
      neurodiversityDetails = supportNeeds.neurodiversityDetails,
      locationTravelDetails = supportNeeds.locationTravelDetails,
      caringResponsibilitiesDetails = supportNeeds.caringResponsibilitiesDetails,
      employmentResponsibilitiesDetails = supportNeeds.employmentResponsibilitiesDetails,
      diversityDetails = supportNeeds.diversityDetails,
      anythingElseDetails = supportNeeds.anythingElseDetails,
      noAdditionalSupportNeeded = supportNeeds.noAdditionalSupportNeeded,
      interpreterLanguage = supportNeeds.interpreterLanguage,
    )

    fun interpreterRequired(supportNeeds: PersonAdditionalSupportNeeds): Boolean = !supportNeeds.interpreterLanguage.isNullOrBlank()
  }
}
