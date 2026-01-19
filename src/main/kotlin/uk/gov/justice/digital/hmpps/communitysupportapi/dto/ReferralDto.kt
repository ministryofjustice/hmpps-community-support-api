package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import uk.gov.justice.digital.hmpps.communitysupportapi.entity.CommunityServiceProvider
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Person
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Referral
import java.util.UUID

data class ReferralDto(
  val id: UUID,
  val crn: String?,
  val referenceNumber: String?,
) {
  companion object {
    fun from(referral: Referral): ReferralDto = ReferralDto(
      id = referral.id,
      crn = referral.crn,
      referenceNumber = referral.referenceNumber,
    )
  }
}

data class ReferralInformationDto(
  val personId: UUID,
  val firstName: String?,
  val lastName: String?,
  val sex: String? = null,
  val crn: String,
  val communityServiceProviderId: UUID,
  val communityServiceProviderName: String,
  val region: String,
  val referenceNumber: String? = null,
  val deliveryPartner: String,
) {
  companion object {
    fun from(result: ReferralCreationResult): ReferralInformationDto = ReferralInformationDto(
      personId = result.referral.personId,
      communityServiceProviderId = result.referral.communityServiceProviderId,
      firstName = result.person.firstName,
      lastName = result.person.lastName,
      sex = result.person.additionalDetails?.sexualOrientation,
      crn = result.referral.crn,
      communityServiceProviderName = result.communityServiceProvider.name,
      region = result.communityServiceProvider.contractArea.region.name,
      deliveryPartner = result.communityServiceProvider.providerName,
      referenceNumber = result.referral.referenceNumber,
    )
  }
}

/**
 * Lightweight result object returned from the service layer so controllers
 * can decide how to convert to DTOs / responses.
 */
data class ReferralCreationResult(
  val referral: Referral,
  val person: Person,
  val communityServiceProvider: CommunityServiceProvider,
)

fun Referral.toDto() = ReferralDto.from(this)
fun ReferralCreationResult.toReferralInformationDto(): ReferralInformationDto = ReferralInformationDto.from(this)
