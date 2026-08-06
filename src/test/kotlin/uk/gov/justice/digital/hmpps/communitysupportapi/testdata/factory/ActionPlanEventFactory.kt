package uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory

import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanEvent
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanEventType
import java.time.OffsetDateTime
import java.util.UUID

/**
 * Factory for creating ActionPlanEvent test entities with sensible defaults.
 * Use the builder pattern to customize individual properties.
 *
 * Example usage:
 * ```
 * // Create with action plan reference
 * val event = ActionPlanEventFactory()
 *     .withActionPlanId(actionPlanId)
 *     .withEventType(ActionPlanEventType.SUBMITTED)
 *     .create()
 *
 * // Create a created event
 * val event = ActionPlanEventFactory.aCreatedEvent(actionPlanId)
 *
 * // Create a submitted event
 * val event = ActionPlanEventFactory.aSubmittedEvent(actionPlanId)
 * ```
 */
class ActionPlanEventFactory : TestEntityFactory<ActionPlanEvent>() {

  private var id: UUID = UUID.randomUUID()
  private var actionPlanId: UUID = UUID.randomUUID()
  private var eventType: ActionPlanEventType = ActionPlanEventType.CREATED
  private var createdAt: OffsetDateTime = OffsetDateTime.now()
  private var createdBy: String = "SYSTEM"

  fun withId(id: UUID) = apply { this.id = id }
  fun withActionPlanId(actionPlanId: UUID) = apply { this.actionPlanId = actionPlanId }
  fun withEventType(eventType: ActionPlanEventType) = apply { this.eventType = eventType }
  fun withCreatedAt(createdAt: OffsetDateTime) = apply { this.createdAt = createdAt }
  fun withCreatedBy(createdBy: String) = apply { this.createdBy = createdBy }

  override fun create(): ActionPlanEvent = ActionPlanEvent(
    id = id,
    actionPlanId = actionPlanId,
    eventType = eventType,
    createdAt = createdAt,
    createdBy = createdBy,
  )

  companion object {
    fun aCreatedEvent(actionPlanId: UUID, createdBy: String = "SYSTEM"): ActionPlanEvent = ActionPlanEventFactory()
      .withActionPlanId(actionPlanId)
      .withEventType(ActionPlanEventType.CREATED)
      .withCreatedBy(createdBy)
      .create()

    fun aSubmittedEvent(actionPlanId: UUID, createdBy: String = "SYSTEM"): ActionPlanEvent = ActionPlanEventFactory()
      .withActionPlanId(actionPlanId)
      .withEventType(ActionPlanEventType.SUBMITTED)
      .withCreatedBy(createdBy)
      .create()
  }
}
