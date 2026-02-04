package uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory

import uk.gov.justice.digital.hmpps.communitysupportapi.entity.CommunityServiceProvider
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Person
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Referral
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralProviderAssignment
import java.time.OffsetDateTime

/**
 * Central access point for all test entity factories.
 * Provides convenient methods to create entities and combines related factory operations.
 *
 * Usage examples:
 * ```
 * // Access individual factories
 * val person = TestEntityFactories.person().withFirstName("Jane").create()
 * val referral = TestEntityFactories.referral().withPersonId(person.id).create()
 *
 * // Use convenience methods
 * val (person, referral) = TestEntityFactories.createPersonWithReferral()
 *
 * // Use static factory methods
 * val person = PersonFactory.aDefaultPerson()
 * val user = ReferralUserFactory.anAuthUser("my-user")
 * ```
 */
object TestEntityFactories {

  fun person(): PersonFactory = PersonFactory()

  fun referral(): ReferralFactory = ReferralFactory()

  fun referralEvent(): ReferralEventFactory = ReferralEventFactory()

  fun referralUser(): ReferralUserFactory = ReferralUserFactory()

  fun referralProviderAssignment(): ReferralProviderAssignmentFactory = ReferralProviderAssignmentFactory()

  fun referralUserAssignment(): ReferralUserAssignmentFactory = ReferralUserAssignmentFactory()

  fun personAdditionalDetails(): PersonAdditionalDetailsFactory = PersonAdditionalDetailsFactory()

  // ==============================
  // Convenience Combination Methods
  // ==============================

  /**
   * Creates a Person with an associated Referral.
   * @return Pair of (Person, Referral)
   */
  fun createPersonWithReferral(
    firstName: String = "John",
    lastName: String = "Doe",
    identifier: String = "CRN${(100000..999999).random()}",
  ): Pair<Person, Referral> {
    val person = person()
      .withFirstName(firstName)
      .withLastName(lastName)
      .withIdentifier(identifier)
      .create()

    val referral = referral()
      .withPersonId(person.id)
      .withCrn(person.identifier)
      .create()

    return Pair(person, referral)
  }

  /**
   * Creates a Person with a submitted Referral.
   * @return Pair of (Person, Referral)
   */
  fun createPersonWithSubmittedReferral(
    firstName: String = "John",
    lastName: String = "Doe",
    identifier: String = "CRN${(100000..999999).random()}",
    actorId: String = "test-user",
  ): Pair<Person, Referral> {
    val person = person()
      .withFirstName(firstName)
      .withLastName(lastName)
      .withIdentifier(identifier)
      .create()

    val referral = referral()
      .withPersonId(person.id)
      .withCrn(person.identifier)
      .withSubmittedEvent(actorId)
      .create()

    return Pair(person, referral)
  }

  /**
   * Creates a complete case with Person, submitted Referral, and provider assignment.
   * @return Triple of (Person, Referral, ReferralProviderAssignment)
   */
  fun createCompleteCase(
    communityServiceProvider: CommunityServiceProvider,
    firstName: String = "John",
    lastName: String = "Doe",
    identifier: String = "CRN${(100000..999999).random()}",
    actorId: String = "test-user",
    referralCreatedAt: OffsetDateTime = OffsetDateTime.now(),
  ): Triple<Person, Referral, ReferralProviderAssignment> {
    val person = person()
      .withFirstName(firstName)
      .withLastName(lastName)
      .withIdentifier(identifier)
      .create()

    val referral = referral()
      .withPersonId(person.id)
      .withCrn(person.identifier)
      .withCreatedAt(referralCreatedAt)
      .withSubmittedEvent(actorId, referralCreatedAt)
      .create()

    val assignment = referralProviderAssignment()
      .withReferral(referral)
      .withCommunityServiceProvider(communityServiceProvider)
      .create()

    return Triple(person, referral, assignment)
  }

  /**
   * Creates multiple complete cases for a provider.
   * @return List of Triple(Person, Referral, ReferralProviderAssignment)
   */
  fun createMultipleCases(
    count: Int,
    communityServiceProvider: CommunityServiceProvider,
    actorId: String = "test-user",
  ): List<Triple<Person, Referral, ReferralProviderAssignment>> = (1..count).map { index ->
    createCompleteCase(
      communityServiceProvider = communityServiceProvider,
      firstName = "FirstName$index",
      lastName = "LastName$index",
      identifier = "CRN${100000 + index}",
      actorId = actorId,
      referralCreatedAt = OffsetDateTime.now().minusDays((index - 1).toLong()),
    )
  }
}
