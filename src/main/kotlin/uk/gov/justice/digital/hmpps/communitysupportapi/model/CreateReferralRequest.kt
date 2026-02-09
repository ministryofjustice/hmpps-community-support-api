package uk.gov.justice.digital.hmpps.communitysupportapi.model

import uk.gov.justice.digital.hmpps.communitysupportapi.dto.PersonDto
import java.util.UUID

data class CreateReferralRequest(
  val personDetails: PersonDto,
  val communityServiceProviderId: UUID,
  val crn: String,
  val urgency: Boolean? = null,
)
