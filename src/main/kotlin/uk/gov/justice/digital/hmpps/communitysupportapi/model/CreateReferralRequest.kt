package uk.gov.justice.digital.hmpps.communitysupportapi.model

data class CreateReferralRequest(
  val personIdentifier: String,
  val urgency: Boolean? = null,
)
