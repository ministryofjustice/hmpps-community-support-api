package uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory

import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanStep
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanStepType
import java.util.UUID

class ActionPlanStepFactory : TestEntityFactory<ActionPlanStep>() {

  private var id: UUID = UUID.randomUUID()
  private var actionPlanTemplateId: UUID = UUID.randomUUID()
  private var orderNumber: Int = 1
  private var name: String = "Test Step"
  private var stepType: ActionPlanStepType = ActionPlanStepType.NEED

  fun withId(id: UUID) = apply { this.id = id }
  fun withActionPlanTemplateId(actionPlanTemplateId: UUID) = apply { this.actionPlanTemplateId = actionPlanTemplateId }
  fun withOrderNumber(orderNumber: Int) = apply { this.orderNumber = orderNumber }
  fun withName(name: String) = apply { this.name = name }
  fun withStepType(stepType: ActionPlanStepType) = apply { this.stepType = stepType }

  override fun create(): ActionPlanStep = ActionPlanStep(
    id = id,
    actionPlanTemplateId = actionPlanTemplateId,
    orderNumber = orderNumber,
    name = name,
    stepType = stepType,
  )
}
