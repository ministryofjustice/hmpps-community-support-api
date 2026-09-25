package uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory

import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanActivity
import java.util.UUID

class ActionPlanActivityFactory : TestEntityFactory<ActionPlanActivity>() {

  private var id: UUID = UUID.randomUUID()
  private var actionPlanStepQuestionAnswerHeaderId: UUID = UUID.randomUUID()
  private var who: String = "Service provider"
  private var activityDetails: String = "Details about the activity"
  private var status: String = "ACTIVE"

  fun withId(id: UUID) = apply { this.id = id }
  fun withActionPlanStepQuestionAnswerHeaderId(actionPlanStepQuestionAnswerHeaderId: UUID) = apply { this.actionPlanStepQuestionAnswerHeaderId = actionPlanStepQuestionAnswerHeaderId }
  fun withWho(who: String) = apply { this.who = who }
  fun withActivityDetails(activityDetails: String) = apply { this.activityDetails = activityDetails }
  fun withStatus(status: String) = apply { this.status = status }

  override fun create(): ActionPlanActivity = ActionPlanActivity(
    id = id,
    actionPlanStepQuestionAnswerHeaderId = actionPlanStepQuestionAnswerHeaderId,
    who = who,
    activityDetails = activityDetails,
    status = status,
  )
}
