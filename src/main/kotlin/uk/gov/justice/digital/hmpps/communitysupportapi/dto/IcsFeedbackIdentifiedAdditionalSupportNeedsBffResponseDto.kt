package uk.gov.justice.digital.hmpps.communitysupportapi.dto

data class IcsFeedbackIdentifiedAdditionalSupportNeedsBffResponseDto(
  val referralFirstName: String,
  val referralSupportNeeds: Map<String, String>,
)
