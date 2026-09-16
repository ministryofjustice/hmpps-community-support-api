package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import java.util.UUID

data class ActionPlanSelectANeedResponse(
  val needs: List<ActionPlanSelectANeedNeed>,
)

data class ActionPlanSelectANeedNeed(
  val id: UUID,
  val label: String,
  val outcomes: List<ActionPlanSelectANeedOutcome>,
)

data class ActionPlanSelectANeedOutcome(
  val id: UUID,
  val text: String,
)
