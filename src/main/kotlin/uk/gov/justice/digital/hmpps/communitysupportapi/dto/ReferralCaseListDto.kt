package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import uk.gov.justice.digital.hmpps.communitysupportapi.entity.CaseListView
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

data class ReferralCaseListDto(
  val referralId: UUID,
  val referenceNumber: String,
  val personName: String,
  val personIdentifier: String,
  val date: String,
  val caseWorkers: List<String> = emptyList(),
) {
  companion object {
    private val DATE_FORMATTER = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH)

    fun from(caseListView: CaseListView): ReferralCaseListDto = ReferralCaseListDto(
      referralId = caseListView.referralId,
      personName = caseListView.personName,
      personIdentifier = caseListView.personIdentifier,
      date = caseListView.dateReceived.format(DATE_FORMATTER),
      caseWorkers = caseListView.caseWorkers,
      referenceNumber = caseListView.referenceNumber,
    )
  }
}
