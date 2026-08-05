package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlan
import java.util.UUID

data class ActionPlanStatusDto(
  val actionPlanId: UUID,
  val status: ActionPlanStatus,
) {
  companion object {
    fun fromActionPlan(actionPlan: ActionPlan, isSubmitted: Boolean): ActionPlanStatusDto = if (isSubmitted) {
      ActionPlanStatusDto(actionPlan.id, ActionPlanStatus.submitted())
    } else {
      ActionPlanStatusDto(actionPlan.id, ActionPlanStatus.notSubmitted())
    }
  }
}

data class ActionPlanStatus(
  val submitted: Boolean,
  val statusText: String,
  val tag: String? = null,
) {
  companion object {
    fun notSubmitted() = ActionPlanStatus(false, "Not Submitted", "govuk-tag--blue")
    fun submitted() = ActionPlanStatus(true, "Submitted", "govuk-tag--teal")
  }
}
