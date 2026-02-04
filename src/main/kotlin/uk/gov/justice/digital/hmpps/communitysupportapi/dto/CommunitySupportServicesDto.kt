package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import uk.gov.justice.digital.hmpps.communitysupportapi.entity.CommunityServiceProvider

data class CommunitySupportServicesDto(
  val personId: String,
  val communitySupportServices: List<CommunitySupportServiceDto>,
) {
  companion object {
    fun from(personId: String, communitySupportServices: List<CommunityServiceProvider>) = CommunitySupportServicesDto(
      personId = personId,
      communitySupportServices = communitySupportServices.map { CommunitySupportServiceDto.from(it) },
    )
  }
}

data class CommunitySupportServiceDto(
  val id: String,
  val region: String,
  val name: String,
  val providerName: String,
  val description: String,
) {
  companion object {
    fun from(communityServiceProvider: CommunityServiceProvider) = CommunitySupportServiceDto(
      id = communityServiceProvider.id.toString(),
      region = communityServiceProvider.contractArea.area,
      name = communityServiceProvider.name,
      providerName = communityServiceProvider.serviceProvider.name,
      description = communityServiceProvider.description,
    )
  }
}
