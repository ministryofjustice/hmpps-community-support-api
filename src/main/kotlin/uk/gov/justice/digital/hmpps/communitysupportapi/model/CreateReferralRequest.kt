package uk.gov.justice.digital.hmpps.communitysupportapi.model

import java.util.UUID

data class CreateReferralRequest(
  val personId: UUID,
  val communityServiceProviderId: UUID,
  val crn: String,
  val urgency: Boolean? = null,
)
