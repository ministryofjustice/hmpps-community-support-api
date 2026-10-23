package uk.gov.justice.digital.hmpps.communitysupportapi.dto

data class SupportNeed(
val needName: String,
val needDetails: String
)

data class ExistingSupportNeedsBffResponseDto(
  val supportNeeds: List<SupportNeed>? = null,
  val referralFirstName: String,
)
