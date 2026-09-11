package uk.gov.justice.digital.hmpps.communitysupportapi.model

import jakarta.validation.constraints.NotBlank
import uk.gov.justice.digital.hmpps.communitysupportapi.validation.NullOrNotBlank

data class WithdrawReferralRequest(
  @field:NotBlank
  val reasonCode: String,
  @field:NullOrNotBlank
  val additionalDetails: String? = null,
) {
  fun normalise(): WithdrawReferralRequest = copy(reasonCode = reasonCode.trim(), additionalDetails = additionalDetails?.trim())
}
