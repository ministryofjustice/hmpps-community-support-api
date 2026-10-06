package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferenceDataCommunityServiceProvider

data class AreaConfirmationBffResponseDto(
  val contractArea: String,
  val deliveryPartner: String,
  val associatedPdus: List<String>,
  val crn: String,
  val dateOfBirth: String,
) {
  companion object {
    fun from(
      referenceDataCommunityServiceProvider: ReferenceDataCommunityServiceProvider,
      associatedPdus: List<String>,
      crn: String,
      dateOfBirth: String,
    ) = AreaConfirmationBffResponseDto(
      contractArea = referenceDataCommunityServiceProvider.referenceDataContractArea.area,
      deliveryPartner = referenceDataCommunityServiceProvider.referenceDataServiceProvider.name,
      associatedPdus = associatedPdus,
      crn = crn,
      dateOfBirth = dateOfBirth,
    )
  }
}
