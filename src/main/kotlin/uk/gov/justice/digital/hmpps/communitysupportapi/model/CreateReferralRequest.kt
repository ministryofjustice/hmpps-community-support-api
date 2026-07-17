package uk.gov.justice.digital.hmpps.communitysupportapi.model

import java.util.UUID

data class CreateReferralRequest(
  val communityServiceProviderId: UUID,
  val personIdentifier: String,
  val urgency: Boolean? = null,
)
