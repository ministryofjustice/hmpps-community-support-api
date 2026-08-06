package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import uk.gov.justice.digital.hmpps.communitysupportapi.entity.CommunityServiceProvider
import java.util.UUID

data class CommunityServiceProviderBffResponseDto(
  val referralId: UUID,
  val communityServiceProviderId: UUID,
  val communityServiceProviderName: String,
) {
  companion object {
    fun from(referralId: UUID, communityServiceProvider: CommunityServiceProvider) = CommunityServiceProviderBffResponseDto(
      referralId = referralId,
      communityServiceProviderId = communityServiceProvider.id,
      communityServiceProviderName = communityServiceProvider.name,
    )
  }
}
