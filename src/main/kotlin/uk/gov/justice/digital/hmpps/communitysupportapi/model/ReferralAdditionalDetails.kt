package uk.gov.justice.digital.hmpps.communitysupportapi.model

import uk.gov.justice.digital.hmpps.communitysupportapi.dto.PersonAdditionalSupportNeedsDto

data class ReferralAdditionalDetails(
  val supportNeeds: PersonAdditionalSupportNeedsDto? = null,
  // future extends structure here
)
