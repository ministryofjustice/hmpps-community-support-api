package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import java.util.UUID

data class ReferralRequest(
  val personId: UUID,
  val communityServiceProviderId: UUID,
  val crn: String,
  val urgency: Boolean = false,
)

typealias CreateReferralRequest = ReferralRequest

typealias CheckReferralInformationRequest = ReferralRequest
