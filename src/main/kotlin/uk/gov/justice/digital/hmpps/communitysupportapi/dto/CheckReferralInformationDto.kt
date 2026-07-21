package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import java.util.UUID

data class CheckReferralInformationDto(
  val referralId: UUID,
  val communityServiceProviderName: String,
  val region: String,
  val deliveryPartner: String,
  val personIdentifier: String?,
  val prisonNumbers: List<String>,
  val fullName: String,
  val dateOfBirth: String,
  val sex: String?,
)
