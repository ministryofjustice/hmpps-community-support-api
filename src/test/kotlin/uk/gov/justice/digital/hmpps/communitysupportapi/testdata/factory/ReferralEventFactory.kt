package uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory

import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActorType
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Referral
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralEvent
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralEventType
import java.time.OffsetDateTime
import java.util.UUID

/**
 * Factory for creating ReferralEvent test entities with sensible defaults.
 * Use the builder pattern to customize individual properties.
 *
 * Example usage:
 * ```
 * // Create with referral reference
 * val event = ReferralEventFactory()
 *     .withReferral(referral)
 *     .withEventType(ReferralEventType.SUBMITTED)
 *     .create()
 *
 * // Create a submitted event
 * val event = ReferralEventFactory.aSubmittedEvent(referral, "test-user")
 * ```
 */
class ReferralEventFactory : TestEntityFactory<ReferralEvent>() {

  private var id: UUID = UUID.randomUUID()
  private lateinit var referral: Referral
  private var eventType: ReferralEventType = ReferralEventType.CREATED
  private var createdAt: OffsetDateTime = OffsetDateTime.now()
  private var actorType: ActorType = ActorType.AUTH
  private var actorId: String = "test-user"

  fun withId(id: UUID) = apply { this.id = id }
  fun withReferral(referral: Referral) = apply { this.referral = referral }
  fun withEventType(eventType: ReferralEventType) = apply { this.eventType = eventType }
  fun withCreatedAt(createdAt: OffsetDateTime) = apply { this.createdAt = createdAt }
  fun withActorType(actorType: ActorType) = apply { this.actorType = actorType }
  fun withActorId(actorId: String) = apply { this.actorId = actorId }

  override fun create(): ReferralEvent {
    check(::referral.isInitialized) { "Referral must be set before creating ReferralEvent" }

    return ReferralEvent(
      id = id,
      referral = referral,
      eventType = eventType,
      createdAt = createdAt,
      actorType = actorType,
      actorId = actorId,
    )
  }

  companion object {
    /**
     * Creates a SUBMITTED event for a referral.
     */
    fun aSubmittedEvent(referral: Referral, actorId: String = "test-user"): ReferralEvent = ReferralEventFactory()
      .withReferral(referral)
      .withEventType(ReferralEventType.SUBMITTED)
      .withActorId(actorId)
      .withCreatedAt(referral.createdAt)
      .create()

    /**
     * Creates a CREATED event for a referral.
     */
    fun aCreatedEvent(referral: Referral, actorId: String = "test-user"): ReferralEvent = ReferralEventFactory()
      .withReferral(referral)
      .withEventType(ReferralEventType.CREATED)
      .withActorId(actorId)
      .create()

    /**
     * Creates an UPDATED event for a referral.
     */
    fun anUpdatedEvent(referral: Referral, actorId: String = "test-user"): ReferralEvent = ReferralEventFactory()
      .withReferral(referral)
      .withEventType(ReferralEventType.UPDATED)
      .withActorId(actorId)
      .create()
  }
}
