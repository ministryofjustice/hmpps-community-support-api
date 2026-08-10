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
import java.util.UUID

enum class ActionPlanStepType {
  NEED,
  CATCH_ALL,
}

@Entity
@Table(name = "action_plan_step")
data class ActionPlanStep(
  @Id
  @Column(name = "id")
  val id: UUID,

  @Column(name = "action_plan_template_id", nullable = false)
  val actionPlanTemplateId: UUID,

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "action_plan_template_id", insertable = false, updatable = false)
  val actionPlanTemplate: ActionPlanTemplate? = null,

  @Column(name = "order_number", nullable = false)
  val orderNumber: Int,

  @Column(name = "name", nullable = false)
  val name: String,

  @Column(name = "step_type", nullable = false)
  @Enumerated(EnumType.STRING)
  val stepType: ActionPlanStepType,

  @Column(name = "need_id")
  val needId: UUID? = null,

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "need_id", insertable = false, updatable = false)
  val need: Need? = null,
)
