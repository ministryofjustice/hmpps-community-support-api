package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import java.util.UUID

class ActionPlanNeedsOrderRequest(
  val stepQuestionAnswerHeaderId: UUID,
  val action: ActionPlanNeedsOrderAction,
) {
  enum class ActionPlanNeedsOrderAction {
    UP,
    DOWN,
  }
}
