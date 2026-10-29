package uk.gov.justice.digital.hmpps.communitysupportapi.dto

data class ExistingSupportNeedsBffResponseDto(
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
