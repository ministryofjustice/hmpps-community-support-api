package uk.gov.justice.digital.hmpps.communitysupportapi.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

/**
 * The ActionPlanTemplate is a holding / container entity for information about
 * the structure of a given ActionPlan, e.g. what questions are posed to the user.
 *
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
