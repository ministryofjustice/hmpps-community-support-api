package uk.gov.justice.digital.hmpps.communitysupportapi.model

import java.util.UUID

sealed class CaseIdentifier {
  data class CaseId(val value: String) : CaseIdentifier()
  data class ReferralId(val value: UUID) : CaseIdentifier()
}
