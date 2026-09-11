package uk.gov.justice.digital.hmpps.communitysupportapi.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

enum class ActionPlanQuestionResponseEventType {
  CREATED,
  UPDATED,
  DELETED,
}

@Entity
@Table(name = "action_plan_question_response_event")
class ActionPlanQuestionResponseEvent(
  @Id
  @Column(name = "id")
  val id: UUID,

  @Column(name = "action_plan_id", nullable = false)
  val actionPlanId: UUID,

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "action_plan_id", insertable = false, updatable = false)
  val actionPlan: ActionPlan? = null,

  @Column(name = "action_plan_step_question_answer_header_id", nullable = false)
  val actionPlanStepQuestionAnswerHeaderId: UUID,

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "action_plan_step_question_answer_header_id", insertable = false, updatable = false)
  val actionPlanStepQuestionAnswerHeader: ActionPlanStepQuestionAnswerHeader? = null,

  @Column(name = "event_type", nullable = false)
  @Enumerated(EnumType.STRING)
  val eventType: ActionPlanQuestionResponseEventType,

  @Column(name = "question_response_change_batch_id")
  val questionResponseChangeBatchId: UUID? = null,

  @Column(name = "created_at", nullable = false)
  val createdAt: OffsetDateTime = OffsetDateTime.now(),

  @Column(name = "created_by", nullable = false)
  val createdBy: String = "SYSTEM",
) {
  companion object {
    fun actionPlanQuestionResponseEventForResponses(
      actionPlanId: UUID,
      responseHeaderId: UUID,
      eventType: ActionPlanQuestionResponseEventType,
      questionResponseChangeBatchId: UUID,
      createdAt: OffsetDateTime = OffsetDateTime.now(),
      createdBy: String = "SYSTEM",
    ): ActionPlanQuestionResponseEvent = ActionPlanQuestionResponseEvent(
      id = UUID.randomUUID(),
      actionPlanId = actionPlanId,
      actionPlanStepQuestionAnswerHeaderId = responseHeaderId,
      eventType = eventType,
      questionResponseChangeBatchId = questionResponseChangeBatchId,
      createdAt = createdAt,
      createdBy = createdBy,
    )
  }
}
