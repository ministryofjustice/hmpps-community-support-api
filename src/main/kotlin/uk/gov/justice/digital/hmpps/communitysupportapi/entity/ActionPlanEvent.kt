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

enum class ActionPlanEventType(val value: String) {
  CREATED("created"),
  SUBMITTED("submitted"),
}

@Entity
@Table(name = "action_plan_event")
class ActionPlanEvent(
  @Id
  @Column(name = "id")
  val id: UUID,

  @Column(name = "action_plan_id", nullable = false)
  val actionPlanId: UUID,

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "action_plan_id", insertable = false, updatable = false)
  val actionPlan: ActionPlan? = null,

  @Column(name = "event_type", nullable = false)
  @Enumerated(EnumType.STRING)
  val eventType: ActionPlanEventType,

  @Column(name = "created_at", nullable = false)
  val createdAt: OffsetDateTime = OffsetDateTime.now(),

  @Column(name = "created_by", nullable = false)
  val createdBy: String = "SYSTEM",
) {
  companion object {
    fun actionPlanCreatedEventForActionPlan(actionPlanId: UUID, time: OffsetDateTime = OffsetDateTime.now()): ActionPlanEvent = ActionPlanEvent(
      id = UUID.randomUUID(),
      actionPlanId = actionPlanId,
      eventType = ActionPlanEventType.CREATED,
      createdAt = time,
    )
  }
}
