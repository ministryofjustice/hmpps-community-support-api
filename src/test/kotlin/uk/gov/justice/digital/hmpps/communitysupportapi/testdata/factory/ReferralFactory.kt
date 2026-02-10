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
 *     .withReferenceNumber("REF-001")
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
  private var crn: String = "CRN${(100000..999999).random()}"
  private var referenceNumber: String? = "REF-${(1000..9999).random()}"
  private var createdAt: OffsetDateTime = OffsetDateTime.now()
  private var updatedAt: OffsetDateTime? = OffsetDateTime.now()
  private var urgency: Boolean? = null
  private var events: MutableList<(Referral) -> ReferralEvent> = mutableListOf()

  fun withId(id: UUID) = apply { this.id = id }
  fun withPersonId(personId: UUID) = apply { this.personId = personId }
  fun withCrn(crn: String) = apply { this.crn = crn }
  fun withReferenceNumber(referenceNumber: String?) = apply { this.referenceNumber = referenceNumber }
  fun withCreatedAt(createdAt: OffsetDateTime) = apply { this.createdAt = createdAt }
  fun withUpdatedAt(updatedAt: OffsetDateTime?) = apply { this.updatedAt = updatedAt }
  fun withUrgency(urgency: Boolean?) = apply { this.urgency = urgency }

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

  /**
   * Adds a custom event to the referral.
   */
  fun withEvent(eventFactory: (Referral) -> ReferralEvent) = apply {
    events.add(eventFactory)
  }

  override fun create(): Referral {
    val referral = Referral(
      id = id,
      personId = personId,
      crn = crn,
      referenceNumber = referenceNumber,
      createdAt = createdAt,
      updatedAt = updatedAt,
      urgency = urgency,
    )

    // Add all configured events
    events.forEach { eventCreator ->
      val event = eventCreator(referral)
      referral.addEvent(event)
    }

    return referral
  }

  companion object {
    fun aDefaultReferral(): Referral = ReferralFactory().create()

    fun aReferralForPerson(personId: UUID, crn: String): Referral = ReferralFactory()
      .withPersonId(personId)
      .withCrn(crn)
      .create()

    fun aSubmittedReferral(personId: UUID, crn: String, actorId: UUID = UUID.randomUUID()): Referral = ReferralFactory()
      .withPersonId(personId)
      .withCrn(crn)
      .withSubmittedEvent(actorId)
      .create()
  }
}
