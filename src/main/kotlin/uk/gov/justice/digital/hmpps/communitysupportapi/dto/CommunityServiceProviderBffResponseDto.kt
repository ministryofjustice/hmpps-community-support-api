package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferenceDataCommunityServiceProvider
import java.util.UUID

data class CommunityServiceProviderBffResponseDto(
  val referralId: UUID,
  val communityServiceProviderId: UUID,
  val communityServiceProviderName: String,
) {
  companion object {
    fun from(referralId: UUID, referenceDataCommunityServiceProvider: ReferenceDataCommunityServiceProvider) = CommunityServiceProviderBffResponseDto(
      referralId = referralId,
      communityServiceProviderId = referenceDataCommunityServiceProvider.id,
      communityServiceProviderName = referenceDataCommunityServiceProvider.name,
    )
  }
}
