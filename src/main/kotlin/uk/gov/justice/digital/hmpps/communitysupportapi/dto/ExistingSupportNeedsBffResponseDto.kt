package uk.gov.justice.digital.hmpps.communitysupportapi.dto
data class SupportNeeds(
  val physicalHealthDetails: String? = null,
  val mentalEmotionalHealthDetails: String? = null,
  val neurodiversityDetails: String? = null,
  val locationTravelDetails: String? = null,
  val caringResponsibilitiesDetails: String? = null,
  val employmentResponsibilitiesDetails: String? = null,
  val diversityDetails: String? = null,
  val anythingElseDetails: String? = null,
  val interpreterLanguage: String? = null,
)

data class ExistingSupportNeedsBffResponseDto(
  val referralFirstName: String,
  val supportNeeds: SupportNeeds,
)
