package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import uk.gov.justice.digital.hmpps.communitysupportapi.entity.CaseListView
import java.time.OffsetDateTime
import java.util.UUID

data class ReferralCaseListDto(
  val referralId: UUID,
  val personName: String,
  val personIdentifier: String,
  val date: OffsetDateTime,
  val caseWorkers: List<String> = emptyList(),
) {
  companion object {
    fun from(caseListView: CaseListView): ReferralCaseListDto = ReferralCaseListDto(
      referralId = caseListView.referralId,
      personName = caseListView.personName,
      personIdentifier = caseListView.personIdentifier,
      date = caseListView.dateAssigned ?: caseListView.dateReceived,
      caseWorkers = caseListView.caseWorkers,
    )
  }
}
