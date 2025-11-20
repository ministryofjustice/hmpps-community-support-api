package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Referral
import java.util.UUID

data class ReferralDto(
  val id: UUID,
  val firstName: String?,
  val lastName: String?,
  val crn: String?,
  val referenceNumber: String?,
) {
  companion object {
    fun from(referral: Referral): ReferralDto = ReferralDto(
      id = referral.id,
      firstName = referral.firstName,
      lastName = referral.lastName,
      crn = referral.crn,
      referenceNumber = referral.referenceNumber,
    )
  }
}
