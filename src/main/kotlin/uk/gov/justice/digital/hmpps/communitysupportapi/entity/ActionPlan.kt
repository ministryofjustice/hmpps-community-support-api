package uk.gov.justice.digital.hmpps.communitysupportapi.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "action_plan")
class ActionPlan(
  @Id
  @Column(name = "id")
  val id: UUID,

  @Column(name = "referral_id", nullable = false)
  val referralId: UUID,

  @Column(name = "action_plan_template_id", nullable = false)
  val actionPlanTemplateId: UUID,

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "referral_id", insertable = false, updatable = false, unique = true)
  val referral: Referral? = null,

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "action_plan_template_id", insertable = false, updatable = false, unique = true)
  val actionPlanTemplate: ActionPlanTemplate? = null,

  @OneToMany(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
  val events: MutableList<ActionPlanEvent> = mutableListOf(),

  @Column(name = "created_at", nullable = false)
  val createdAt: OffsetDateTime = OffsetDateTime.now(),

  @Column(name = "updated_at")
  val updatedAt: OffsetDateTime = OffsetDateTime.now(),
) {
  companion object {
    fun forReferral(actionPlanTemplateId: UUID, referralId: UUID): ActionPlan = ActionPlan(
      id = UUID.randomUUID(),
      actionPlanTemplateId = actionPlanTemplateId,
      referralId = referralId,
    )
  }

  fun isSubmitted(): Boolean = events
    .map { it.eventType }
    .contains(ActionPlanEventType.SUBMITTED)
}
