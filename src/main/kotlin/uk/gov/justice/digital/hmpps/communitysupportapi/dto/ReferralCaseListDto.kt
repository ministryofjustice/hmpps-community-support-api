package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import uk.gov.justice.digital.hmpps.communitysupportapi.entity.CaseListView
import java.time.OffsetDateTime
import java.util.UUID

data class ReferralCaseListDto(
  val referralId: UUID,
  val personName: String,
  val personIdentifier: String,
  val dateReceived: OffsetDateTime,
) {
  companion object {
    fun from(caseListView: CaseListView): ReferralCaseListDto = ReferralCaseListDto(
      referralId = caseListView.referralId,
      personName = caseListView.personName,
      personIdentifier = caseListView.personIdentifier,
      dateReceived = caseListView.dateReceived,
    )
  }
}
