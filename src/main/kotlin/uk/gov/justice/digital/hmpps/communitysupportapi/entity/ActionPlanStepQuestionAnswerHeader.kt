package uk.gov.justice.digital.hmpps.communitysupportapi.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

/**
 * The stable record for one ActionPlan's answer to a Question, i.e. this is
 * similar to a join table between an ActionPlan and a StepQuestion.
 *
 * The actual "real" answer is given in the ActionPlanStepQuestionAnswerDetails
 *
 * This pattern allows us to keep a full history of answers to a question, while
 * also allowing other entities to reference the answer to a specific question without
 * referencing a *specific* version of that answer.
 *
 * We expect changes to questions to be frequent enough, and other structured data
 * around QuestionAnswers to be important enough to necessitate both stable references
 * but also a full history.
 *
 * @see ActionPlanStepQuestionAnswerDetails
 */
@Entity
@Table(name = "action_plan_step_question_answer_header")
data class ActionPlanStepQuestionAnswerHeader(
  @Id
  @Column(name = "id")
  val id: UUID,

  @Column(name = "action_plan_id", nullable = false)
  val actionPlanId: UUID,

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "action_plan_id", insertable = false, updatable = false)
  val actionPlan: ActionPlan? = null,

  @Column(name = "action_plan_step_question_id", nullable = false)
  val actionPlanStepQuestionId: UUID,

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "action_plan_step_question_id", insertable = false, updatable = false)
  val actionPlanStepQuestion: ActionPlanStepQuestion? = null,

  @Column(name = "order_number", nullable = false)
  val orderNumber: Int,

  @OneToMany(fetch = FetchType.LAZY)
  @JoinColumn(name = "action_plan_step_question_answer_header_id", insertable = false, updatable = false)
  val details: MutableList<ActionPlanStepQuestionAnswerDetails> = mutableListOf(),

  @Column(name = "created_at", nullable = false)
  val createdAt: OffsetDateTime? = null,

  @Column(name = "created_by", nullable = false)
  val createdBy: String = "SYSTEM",

  @Column(name = "deleted_at")
  val deletedAt: OffsetDateTime? = null,

  @Column(name = "deleted_by")
  val deletedBy: String? = null,
) {
  companion object {
    fun from(
      actionPlanId: UUID,
      questionId: UUID,
      createdBy: String,
      createdAt: OffsetDateTime = OffsetDateTime.now(),
    ): ActionPlanStepQuestionAnswerHeader = ActionPlanStepQuestionAnswerHeader(
      id = UUID.randomUUID(),
      actionPlanId = actionPlanId,
      actionPlanStepQuestionId = questionId,
      createdAt = createdAt,
      createdBy = createdBy,
      // Come back and delete
      orderNumber = 0,
    )
  }
}
