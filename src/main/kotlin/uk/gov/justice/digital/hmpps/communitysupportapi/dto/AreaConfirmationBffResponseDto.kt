package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import uk.gov.justice.digital.hmpps.communitysupportapi.entity.CommunityServiceProvider

data class AreaConfirmationBffResponseDto(
  val contractArea: String,
  val deliveryPartner: String,
  val associatedPdus: List<String>,
  val crn: String,
  val dateOfBirth: String,
) {
  companion object {
    fun from(
      communityServiceProvider: CommunityServiceProvider,
      associatedPdus: List<String>,
      crn: String,
      dateOfBirth: String,
    ) = AreaConfirmationBffResponseDto(
      contractArea = communityServiceProvider.contractArea.area,
      deliveryPartner = communityServiceProvider.serviceProvider.name,
      associatedPdus = associatedPdus,
      crn = crn,
      dateOfBirth = dateOfBirth,
    )
  }
}
