package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanActivity
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanStepQuestionAnswerDetails
import java.util.UUID

data class ActionPlanSummaryDto(
  val personDetails: ActionPlanSummaryPersonDetails,
  val needs: List<ActionPlanSummaryNeed>,
) {
  data class ActionPlanSummaryPersonDetails(
    val firstName: String,
    val lastName: String,
  )

  data class ActionPlanSummaryNeed(
    val id: UUID,
    val label: String,
    val outcomes: List<ActionPlanSummaryOutcome> = emptyList(),
  )

  data class ActionPlanSummaryOutcome(
    val id: UUID,
    val label: String,
    val activities: List<ActionPlanSummaryOutcomeActivity> = emptyList(),
  ) {
    companion object {
      fun from(actionPlanStepQuestionAnswerDetails: ActionPlanStepQuestionAnswerDetails, actionPlanActivities: Collection<ActionPlanActivity>): ActionPlanSummaryOutcome = ActionPlanSummaryOutcome(
        actionPlanStepQuestionAnswerDetails.id,
        label = actionPlanStepQuestionAnswerDetails.content.orEmpty(),
        activities = actionPlanActivities.map { activity -> ActionPlanSummaryOutcomeActivity.from(activity) },
      )
    }
  }

  data class ActionPlanSummaryOutcomeActivity(
    val id: UUID,
    val who: String,
    val details: String,
  ) {
    companion object {
      fun from(actionPlanActivity: ActionPlanActivity): ActionPlanSummaryOutcomeActivity = ActionPlanSummaryOutcomeActivity(
        actionPlanActivity.id,
        actionPlanActivity.who,
        details = actionPlanActivity.activityDetails,
      )
    }
  }
}
