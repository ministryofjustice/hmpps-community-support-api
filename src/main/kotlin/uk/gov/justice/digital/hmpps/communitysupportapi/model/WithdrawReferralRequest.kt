package uk.gov.justice.digital.hmpps.communitysupportapi.model

import uk.gov.justice.digital.hmpps.communitysupportapi.validation.NullOrNotBlank

enum class ReferralWithdrawalReasonCode {
  INELIGIBLE_REFERRAL,
  MISTAKEN_OR_DUPLICATE_REFERRAL,
  NOT_ENGAGED,
  NEEDS_MET_THROUGH_ANOTHER_ROUTE,
  USER_DIED,
  WORK_CARING_COMMITMENTS_OR_SICKNESS,
  ACQUITTED_ON_APPEAL,
  RETURNED_TO_CUSTODY,
  SENTENCE_REVOKED,
  SENTENCE_EXPIRED,
  OTHER_CHANGE_OF_CIRCUMSTANCE,
}

data class WithdrawReferralRequest(
  val reasonCode: ReferralWithdrawalReasonCode,
  @field:NullOrNotBlank
  val additionalDetails: String? = null,
) {
  fun normalise(): WithdrawReferralRequest = copy(additionalDetails = additionalDetails?.trim())
}
