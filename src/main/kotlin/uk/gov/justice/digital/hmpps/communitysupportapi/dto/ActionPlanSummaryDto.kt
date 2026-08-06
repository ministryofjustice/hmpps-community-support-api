package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import java.util.UUID

data class ActionPlanSummaryDto(
  val personDetails: ActionPlanSummaryPersonDetails,
  val needs: List<ActionPlanSummaryNeed>,
) {
  data class ActionPlanSummaryPersonDetails(
    val fullName: String,
  )

  data class ActionPlanSummaryNeed(
    val id: UUID,
    val label: String,
    val outcomes: List<String> = emptyList(),
  )
}
