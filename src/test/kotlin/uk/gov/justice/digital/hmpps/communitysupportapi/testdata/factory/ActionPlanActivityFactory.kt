package uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory

import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanActivity
import java.util.UUID

class ActionPlanActivityFactory : TestEntityFactory<ActionPlanActivity>() {
  private var id: UUID = UUID.randomUUID()
  private var actionPlanStepQuestionId: UUID = UUID.randomUUID()
  private var who: String = "SYSTEM"
  private var activityDetails: String = "Activity details"
  private var status: String = "OPEN"

  fun withId(id: UUID) = apply { this.id = id }
  fun withActionPlanStepQuestionId(actionPlanStepQuestionId: UUID) = apply { this.actionPlanStepQuestionId = actionPlanStepQuestionId }
  fun withWho(who: String) = apply { this.who = who }
  fun withActivityDetails(activityDetails: String) = apply { this.activityDetails = activityDetails }
  fun withStatus(status: String) = apply { this.status = status }

  override fun create(): ActionPlanActivity = ActionPlanActivity(
    id = id,
    actionPlanStepQuestionId = actionPlanStepQuestionId,
    who = who,
    activityDetails = activityDetails,
    status = status,
  )
}
