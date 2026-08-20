package uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory

import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanStepQuestionChoice
import java.time.OffsetDateTime
import java.util.UUID

class ActionPlanStepQuestionChoiceFactory : TestEntityFactory<ActionPlanStepQuestionChoice>() {

  private var id: UUID = UUID.randomUUID()
  private var actionPlanStepQuestionId: UUID = UUID.randomUUID()
  private var orderNumber: Int = 1
  private var label: String = "Test Choice"
  private var value: String = "test-value"
  private var hasFreeText: Boolean = false
  private var freeTextLabel: String? = null
  private var createdAt: OffsetDateTime? = OffsetDateTime.now()
  private var createdBy: String = "SYSTEM"

  fun withId(id: UUID) = apply { this.id = id }

  fun withActionPlanStepQuestionId(id: UUID) = apply { this.actionPlanStepQuestionId = id }

  fun withOrderNumber(orderNumber: Int) = apply { this.orderNumber = orderNumber }

  fun withLabel(label: String) = apply { this.label = label }

  fun withValue(value: String) = apply { this.value = value }

  fun withHasFreeText(hasFreeText: Boolean) = apply { this.hasFreeText = hasFreeText }

  fun withFreeTextLabel(freeTextLabel: String?) = apply { this.freeTextLabel = freeTextLabel }

  fun withCreatedAt(createdAt: OffsetDateTime?) = apply { this.createdAt = createdAt }

  fun withCreatedBy(createdBy: String) = apply { this.createdBy = createdBy }

  override fun create(): ActionPlanStepQuestionChoice = ActionPlanStepQuestionChoice(
    id = id,
    actionPlanStepQuestionId = actionPlanStepQuestionId,
    orderNumber = orderNumber,
    label = label,
    value = value,
    hasFreeText = hasFreeText,
    freeTextLabel = freeTextLabel,
    createdAt = createdAt,
    createdBy = createdBy,
  )
}
