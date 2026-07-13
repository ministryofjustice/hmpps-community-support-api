package uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory

import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Referral
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralEvent
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralEventType
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.ReferralEventFactory.Companion.DEFAULT_ACTOR_ID
import java.time.OffsetDateTime
import java.util.UUID

/**
 * Factory for creating Referral test entities with sensible defaults.
 * Use the builder pattern to customize individual properties.
 *
 * Example usage:
 * ```
 * // Create with all defaults
 * val referral = ReferralFactory().create()
 *
 * // Create with custom values
 * val referral = ReferralFactory()
 *     .withPersonId(person.id)
 *     .withCrn("CRN123456")
 *     .withReferenceNumber("AB1234CD")
 *     .create()
 *
 * // Create with a submitted event
 * val referral = ReferralFactory()
 *     .withPersonId(person.id)
 *     .withSubmittedEvent("test-user")
 *     .create()
 * ```
 */
class ReferralFactory : TestEntityFactory<Referral>() {

  private var id: UUID = UUID.randomUUID()
  private var personId: UUID = UUID.randomUUID()
  private var personIdentifier: String = "CRN${(100000..999999).random()}"
  private var referenceNumber: String? = "AA${(1000..9999).random()}BB"
  private var createdAt: OffsetDateTime = OffsetDateTime.now()
  private var updatedAt: OffsetDateTime? = OffsetDateTime.now()
  private var urgency: Boolean? = null
  private var events: MutableList<(Referral) -> ReferralEvent> = mutableListOf()
  private var createdBy: UUID = UUID.randomUUID()

  fun withId(id: UUID) = apply { this.id = id }
  fun withPersonId(personId: UUID) = apply { this.personId = personId }
  fun withCrn(crn: String) = apply { this.personIdentifier = crn }
  fun withReferenceNumber(referenceNumber: String?) = apply { this.referenceNumber = referenceNumber }
  fun withCreatedAt(createdAt: OffsetDateTime) = apply { this.createdAt = createdAt }
  fun withUpdatedAt(updatedAt: OffsetDateTime?) = apply { this.updatedAt = updatedAt }
  fun withUrgency(urgency: Boolean?) = apply { this.urgency = urgency }
  fun withCreatedBy(createdBy: UUID) = apply { this.createdBy = createdBy }

  fun withCreatedEvent(actorId: UUID = DEFAULT_ACTOR_ID, createdAt: OffsetDateTime? = null) = apply {
    events.add { referral ->
      ReferralEventFactory()
        .withReferral(referral)
        .withEventType(ReferralEventType.CREATED)
        .withActorId(actorId)
        .withCreatedAt(createdAt ?: referral.createdAt)
        .create()
    }
  }

  fun withSubmittedEvent(actorId: UUID = DEFAULT_ACTOR_ID, createdAt: OffsetDateTime? = null) = apply {
    events.add { referral ->
      ReferralEventFactory()
        .withReferral(referral)
        .withEventType(ReferralEventType.SUBMITTED)
        .withActorId(actorId)
        .withCreatedAt(createdAt ?: referral.createdAt)
        .create()
    }
  }

  override fun create(): Referral {
    val referral = Referral(
      id = id,
      personId = personId,
      personIdentifier = personIdentifier,
      referenceNumber = referenceNumber,
      createdAt = createdAt,
      updatedAt = updatedAt,
      urgency = urgency,
      createdBy = createdBy,
    )

    // Add all configured events
    events.forEach { eventCreator ->
      val event = eventCreator(referral)
      referral.addEvent(event)
    }

    return referral
  }
}
