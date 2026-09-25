package uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory

import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanStepQuestionAnswerHeader
import java.time.OffsetDateTime
import java.util.UUID

class ActionPlanStepQuestionAnswerHeaderFactory : TestEntityFactory<ActionPlanStepQuestionAnswerHeader>() {

  private var id: UUID = UUID.randomUUID()
  private var actionPlanId: UUID = UUID.randomUUID()
  private var actionPlanStepQuestionId: UUID = UUID.randomUUID()
  private var orderNumber: Int = 1
  private var createdAt: OffsetDateTime = OffsetDateTime.now()
  private var createdBy: String = "Service provider"
  private var deletedAt: OffsetDateTime? = null
  private var deletedBy: String? = null

  fun withId(id: UUID) = apply { this.id = id }
  fun withActionPlanId(actionPlanId: UUID) = apply { this.actionPlanId = actionPlanId }
  fun withActionPlanStepQuestionId(actionPlanStepQuestionId: UUID) = apply { this.actionPlanStepQuestionId = actionPlanStepQuestionId }
  fun withOrderNumber(orderNumber: Int) = apply { this.orderNumber = orderNumber }
  fun withCreatedAt(createdAt: OffsetDateTime) = apply { this.createdAt = createdAt }
  fun withCreatedBy(createdBy: String) = apply { this.createdBy = createdBy }
  fun withDeletedAt(deletedAt: OffsetDateTime?) = apply { this.deletedAt = deletedAt }
  fun withDeletedBy(deletedBy: String?) = apply { this.deletedBy = deletedBy }

  override fun create(): ActionPlanStepQuestionAnswerHeader = ActionPlanStepQuestionAnswerHeader(
    id = id,
    actionPlanId = actionPlanId,
    actionPlanStepQuestionId = actionPlanStepQuestionId,
    orderNumber = orderNumber,
    createdAt = createdAt,
    createdBy = createdBy,
    deletedAt = deletedAt,
    deletedBy = deletedBy,
  )
}
