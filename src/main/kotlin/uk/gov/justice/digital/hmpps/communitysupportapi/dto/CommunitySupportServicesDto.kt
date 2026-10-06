package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferenceDataCommunityServiceProvider

data class CommunitySupportServicesDto(
  val communitySupportServices: Map<String, List<CommunitySupportServiceDto>>,
)

data class CommunitySupportServiceDto(
  val id: String,
  val region: String,
  val area: String,
  val name: String,
  val providerName: String,
  val description: String,
  val pdus: List<String>,
) {
  companion object {
    fun from(referenceDataCommunityServiceProvider: ReferenceDataCommunityServiceProvider, pdus: List<String>) = CommunitySupportServiceDto(
      id = referenceDataCommunityServiceProvider.id.toString(),
      region = referenceDataCommunityServiceProvider.referenceDataContractArea.referenceDataRegion.name,
      area = referenceDataCommunityServiceProvider.referenceDataContractArea.area,
      name = referenceDataCommunityServiceProvider.name,
      providerName = referenceDataCommunityServiceProvider.referenceDataServiceProvider.name,
      description = referenceDataCommunityServiceProvider.description,
      pdus = pdus,
    )
  }
}
