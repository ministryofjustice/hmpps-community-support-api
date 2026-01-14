package uk.gov.justice.digital.hmpps.communitysupportapi.dto

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
  val deliveryPartner: String,
)
