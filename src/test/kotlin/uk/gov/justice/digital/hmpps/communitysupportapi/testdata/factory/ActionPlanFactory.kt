package uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory

import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlan
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanEvent
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanEventType
import java.time.OffsetDateTime
import java.util.UUID

/**
 * Factory for creating ActionPlan test entities with sensible defaults.
 * Use the builder pattern to customize individual properties.
 *
 * Example usage:
 * ```
 * // Create with defaults
 * val actionPlan = ActionPlanFactory().create()
 *
 * // Create with custom values
 * val actionPlan = ActionPlanFactory()
 *     .withReferralId(referralId)
 *     .withActionPlanTemplateId(templateId)
 *     .withCreatedEvent()
 *     .create()
 *
 * // Create with a submitted event
 * val actionPlan = ActionPlanFactory()
 *     .withReferralId(referralId)
 *     .withActionPlanTemplateId(templateId)
 *     .withCreatedEvent()
 *     .withSubmittedEvent()
 *     .create()
 * ```
 */
class ActionPlanFactory : TestEntityFactory<ActionPlan>() {

  private var id: UUID = UUID.randomUUID()
  private var referralId: UUID = UUID.randomUUID()
  private var actionPlanTemplateId: UUID = UUID.randomUUID()
  private var createdAt: OffsetDateTime = OffsetDateTime.now()
  private var updatedAt: OffsetDateTime = OffsetDateTime.now()
  private var events: MutableList<(UUID) -> ActionPlanEvent> = mutableListOf()

  fun withId(id: UUID) = apply { this.id = id }
  fun withReferralId(referralId: UUID) = apply { this.referralId = referralId }
  fun withActionPlanTemplateId(actionPlanTemplateId: UUID) = apply { this.actionPlanTemplateId = actionPlanTemplateId }
  fun withCreatedAt(createdAt: OffsetDateTime) = apply { this.createdAt = createdAt }
  fun withUpdatedAt(updatedAt: OffsetDateTime) = apply { this.updatedAt = updatedAt }

  fun withCreatedEvent(createdBy: String = "SYSTEM", createdAt: OffsetDateTime? = null) = apply {
    events.add { actionPlanId ->
      ActionPlanEventFactory()
        .withActionPlanId(actionPlanId)
        .withEventType(ActionPlanEventType.CREATED)
        .withCreatedBy(createdBy)
        .withCreatedAt(createdAt ?: this.createdAt)
        .create()
    }
  }

  fun withSubmittedEvent(createdBy: String = "SYSTEM", createdAt: OffsetDateTime? = null) = apply {
    events.add { actionPlanId ->
      ActionPlanEventFactory()
        .withActionPlanId(actionPlanId)
        .withEventType(ActionPlanEventType.SUBMITTED)
        .withCreatedBy(createdBy)
        .withCreatedAt(createdAt ?: this.createdAt)
        .create()
    }
  }

  override fun create(): ActionPlan {
    val actionPlan = ActionPlan(
      id = id,
      referralId = referralId,
      actionPlanTemplateId = actionPlanTemplateId,
      createdAt = createdAt,
      updatedAt = updatedAt,
    )

    // Add all configured events
    events.forEach { eventCreator ->
      val event = eventCreator(actionPlan.id)
      actionPlan.events.add(event)
    }

    return actionPlan
  }

  companion object {
    /**
     * Creates an action plan for a referral with a created event.
     */
    fun anActionPlanForReferral(referralId: UUID, templateId: UUID = UUID.randomUUID()): ActionPlan = ActionPlanFactory()
      .withReferralId(referralId)
      .withActionPlanTemplateId(templateId)
      .withCreatedEvent()
      .create()

    /**
     * Creates a submitted action plan for a referral.
     */
    fun aSubmittedActionPlanForReferral(referralId: UUID, templateId: UUID = UUID.randomUUID()): ActionPlan = ActionPlanFactory()
      .withReferralId(referralId)
      .withActionPlanTemplateId(templateId)
      .withCreatedEvent()
      .withSubmittedEvent()
      .create()
  }
}
