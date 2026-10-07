package uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory

import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanStepType
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferenceDataActionPlanStep
import java.util.UUID

class ActionPlanStepFactory : TestEntityFactory<ReferenceDataActionPlanStep>() {

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
  fun ofSessionDeliveryType() = apply { this.stepType = ActionPlanStepType.SESSION_DELIVERY }

  override fun create(): ReferenceDataActionPlanStep = ReferenceDataActionPlanStep(
    id = id,
    actionPlanTemplateId = actionPlanTemplateId,
    orderNumber = orderNumber,
    name = name,
    stepType = stepType,
  )
}
