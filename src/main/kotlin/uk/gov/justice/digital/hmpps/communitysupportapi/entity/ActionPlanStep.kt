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

/**
 * The possible types of Step in an ActionPlan, each corresponding, roughly to a screen
 * in the UI.
 */
enum class ActionPlanStepType {
  NEED,
  SESSION_DELIVERY,
  CATCH_ALL,
}

/**
 * A holding / container entity for related information, in the form of a Question
 * (ActionPlanStepQuestion), defined in a template (ActionPlanTemplate), and applied
 * to a specific ActionPlan
 *
 * At time of authorship, each "Step" refers to a screen (or multi-screen step) in the
 * UI.
 *
 * Modelling this allows the API to collate relevant questions and information for an ActionPlan
 * in the same way that a user would, and therefore to feed data for specific screens in a structured
 * way.
 *
 * @see ActionPlanStepQuestion
 * @see ActionPlan
 */
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
)
