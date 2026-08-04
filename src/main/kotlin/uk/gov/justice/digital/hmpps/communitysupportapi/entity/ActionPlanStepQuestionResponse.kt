package uk.gov.justice.digital.hmpps.communitysupportapi.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "action_plan_step_question_response")
data class ActionPlanStepQuestionResponse(
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

  @Column(name = "response")
  val response: String? = null,

  @Column(name = "created_at", nullable = false)
  val createdAt: OffsetDateTime? = null,

  @Column(name = "created_by", nullable = false)
  val createdBy: String = "SYSTEM",
)
