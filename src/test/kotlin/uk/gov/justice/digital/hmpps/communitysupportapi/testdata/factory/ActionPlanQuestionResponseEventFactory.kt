package uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory

import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanQuestionResponseEvent
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanQuestionResponseEventType
import java.time.OffsetDateTime
import java.util.UUID

class ActionPlanQuestionResponseEventFactory : TestEntityFactory<ActionPlanQuestionResponseEvent>() {

  private var id: UUID = UUID.randomUUID()
  private var actionPlanId: UUID = UUID.randomUUID()
  private var actionPlanStepQuestionAnswerHeaderId: UUID = UUID.randomUUID()
  private var eventType: ActionPlanQuestionResponseEventType = ActionPlanQuestionResponseEventType.CREATED
  private var questionResponseChangeBatchId: UUID = UUID.randomUUID()
  private var createdAt: OffsetDateTime = OffsetDateTime.now()
  private var createdBy: String = "SYSTEM"

  fun withId(id: UUID) = apply { this.id = id }
  fun withActionPlanId(actionPlanId: UUID) = apply { this.actionPlanId = actionPlanId }
  fun withActionPlanStepQuestionAnswerHeaderId(actionPlanStepQuestionAnswerHeaderId: UUID) = apply { this.actionPlanStepQuestionAnswerHeaderId = actionPlanStepQuestionAnswerHeaderId }
  fun withEventType(eventType: ActionPlanQuestionResponseEventType) = apply { this.eventType = eventType }
  fun withQuestionResponseChangeBatchId(questionResponseChangeBatchId: UUID) = apply { this.questionResponseChangeBatchId = questionResponseChangeBatchId }
  fun withCreatedAt(createdAt: OffsetDateTime) = apply { this.createdAt = createdAt }
  fun withCreatedBy(createdBy: String) = apply { this.createdBy = createdBy }

  override fun create(): ActionPlanQuestionResponseEvent = ActionPlanQuestionResponseEvent(
    id = id,
    actionPlanId = actionPlanId,
    actionPlanStepQuestionAnswerHeaderId = actionPlanStepQuestionAnswerHeaderId,
    eventType = eventType,
    questionResponseChangeBatchId = questionResponseChangeBatchId,
    createdAt = createdAt,
    createdBy = createdBy,
  )
}
