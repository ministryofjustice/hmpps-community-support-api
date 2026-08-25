package uk.gov.justice.digital.hmpps.communitysupportapi.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

/**
 * The ActionPlanTemplate is a holding / container entity for information about
 * the required information for any given ActionPlan.
 * At the time of writing, all ActionPlans will follow the same template, and therefore
 * the `active_global` template should be the only ActionPlanTemplate instance
 *
 * @see ActionPlan
 * @see ActionPlanStep
 */
@Entity
@Table(name = "action_plan_template")
data class ActionPlanTemplate(
  @Id
  @Column(name = "id")
  val id: UUID,

  @Column(name = "active_global")
  val activeGlobal: Boolean = false,
)
