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
 * The stable record for one answer to a question in an action plan.
 *
 * Other records can refer to this header without being tied to a particular
 * version of the answer. The answer's changing content is stored in its details.
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
)
