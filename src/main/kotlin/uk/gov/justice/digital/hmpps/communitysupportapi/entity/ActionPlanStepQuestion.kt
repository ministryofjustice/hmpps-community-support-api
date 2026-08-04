package uk.gov.justice.digital.hmpps.communitysupportapi.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "action_plan_step_question")
data class ActionPlanStepQuestion(
  @Id
  @Column(name = "id")
  val id: UUID,

  @Column(name = "action_plan_step_id", nullable = false)
  val actionPlanStepId: UUID,

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "action_plan_step_id", insertable = false, updatable = false)
  val actionPlanStep: ActionPlanStep? = null,

  @Column(name = "order_number", nullable = false)
  val orderNumber: Int,

  @Column(name = "title", nullable = false)
  val title: String,

  @Column(name = "answer_type", nullable = false)
  val answerType: String,
)
