package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import uk.gov.justice.digital.hmpps.communitysupportapi.entity.CommunityServiceProvider

data class CommunitySupportServicesDto(
  val communitySupportServices: List<CommunitySupportServiceDto>,
)

data class CommunitySupportServiceDto(
  val id: String,
  val region: String,
  val name: String,
  val providerName: String,
  val description: String,
  val pdus: List<String>,
) {
  companion object {
    fun from(communityServiceProvider: CommunityServiceProvider, pdus: List<String>) = CommunitySupportServiceDto(
      id = communityServiceProvider.id.toString(),
      region = communityServiceProvider.contractArea.area,
      name = communityServiceProvider.name,
      providerName = communityServiceProvider.serviceProvider.name,
      description = communityServiceProvider.description,
      pdus = pdus,
    )
  }
}
