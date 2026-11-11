package uk.gov.justice.digital.hmpps.communitysupportapi.repository

import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.ReferralTestSupport
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.ReferralNonContactSessionFactory
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.ReferralNonContactSessionReferenceDataReferralPathwayFactory
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

class ReferralNonContactSessionRepositoryTest : IntegrationTestBase() {

  @Autowired
  private lateinit var referralNonContactSessionRepository: ReferralNonContactSessionRepository

  @Autowired
  private lateinit var referralNonContactSessionReferenceDataReferralPathwayRepository: ReferralNonContactSessionReferenceDataReferralPathwayRepository

  @Autowired
  private lateinit var referralHelper: ReferralTestSupport

  @Autowired
  private lateinit var entityManager: EntityManager

  @Test
  @Transactional
  fun `should load referral non contact sessions with correct pathways`() {
    val referral = referralHelper.createReferral()

    val referralNonContactSession = referralNonContactSessionRepository.saveAndFlush(
      ReferralNonContactSessionFactory()
        .withReferralId(referral.id)
        .withStartDateTime(OffsetDateTime.of(2026, 11, 5, 13, 30, 0, 0, ZoneOffset.UTC))
        .withDurationHours(0)
        .withDurationMinutes(10)
        .withTitle("Test title")
        .withDetails("Test details")
        .create(),
    )

    referralNonContactSessionReferenceDataReferralPathwayRepository.saveAndFlush(
      ReferralNonContactSessionReferenceDataReferralPathwayFactory()
        .withReferralNonContactSessionId(referralNonContactSession.id)
        .withReferenceDataReferralPathwayId(UUID.fromString("ca4d147f-cbc3-4c2d-9e33-627c9974d802"))
        .create(),
    )

    referralNonContactSessionReferenceDataReferralPathwayRepository.saveAndFlush(
      ReferralNonContactSessionReferenceDataReferralPathwayFactory()
        .withReferralNonContactSessionId(referralNonContactSession.id)
        .withReferenceDataReferralPathwayId(UUID.fromString("ca4d147f-cbc3-4c2d-9e33-627c9974d803"))
        .create(),
    )

    entityManager.clear()

    val nonContactSessions = referralNonContactSessionRepository.findAllByReferralIdAndDeletedAtIsNullOrderByCreatedAtDesc(referral.id)

    assertEquals(1, nonContactSessions.size)

    val nonContactSession = nonContactSessions.first()
    assertEquals(referral.id, nonContactSession.referralId)
    assertEquals(OffsetDateTime.of(2026, 11, 5, 13, 30, 0, 0, ZoneOffset.UTC), nonContactSession.startDateTime)
    assertEquals(0, nonContactSession.durationHours)
    assertEquals(10, nonContactSession.durationMinutes)
    assertEquals("Test title", nonContactSession.title)
    assertEquals("Test details", nonContactSession.details)

    val pathways = nonContactSession.referralPathways
    assertEquals(2, pathways.size)

    val firstPathway = pathways[0]
    val secondPathway = pathways[1]
    assertEquals(UUID.fromString("ca4d147f-cbc3-4c2d-9e33-627c9974d802"), firstPathway.referenceDataReferralPathwayId)
    assertEquals("Housing support", firstPathway.referenceDataReferralPathway!!.title)
    assertEquals(UUID.fromString("ca4d147f-cbc3-4c2d-9e33-627c9974d803"), secondPathway.referenceDataReferralPathwayId)
    assertEquals("Jobs and skills support", secondPathway.referenceDataReferralPathway!!.title)
  }

  @Test
  @Transactional
  fun `should load referral non contact session in order of createdAt time descending`() {
    val referral = referralHelper.createReferral()

    val firstReferralNonContactSession = referralNonContactSessionRepository.saveAndFlush(
      ReferralNonContactSessionFactory()
        .withReferralId(referral.id)
        .withStartDateTime(OffsetDateTime.of(2026, 11, 5, 13, 30, 0, 0, ZoneOffset.UTC))
        .withDurationHours(0)
        .withDurationMinutes(10)
        .withTitle("First title")
        .withDetails("First details")
        .create(),
    )

    val secondReferralNonContactSession = referralNonContactSessionRepository.saveAndFlush(
      ReferralNonContactSessionFactory()
        .withReferralId(referral.id)
        .withStartDateTime(OffsetDateTime.of(2026, 11, 5, 13, 30, 0, 0, ZoneOffset.UTC))
        .withDurationHours(0)
        .withDurationMinutes(10)
        .withTitle("Second title")
        .withDetails("Second details")
        .create(),
    )

    referralNonContactSessionReferenceDataReferralPathwayRepository.saveAndFlush(
      ReferralNonContactSessionReferenceDataReferralPathwayFactory()
        .withReferralNonContactSessionId(firstReferralNonContactSession.id)
        .withReferenceDataReferralPathwayId(UUID.fromString("ca4d147f-cbc3-4c2d-9e33-627c9974d802"))
        .create(),
    )

    referralNonContactSessionReferenceDataReferralPathwayRepository.saveAndFlush(
      ReferralNonContactSessionReferenceDataReferralPathwayFactory()
        .withReferralNonContactSessionId(secondReferralNonContactSession.id)
        .withReferenceDataReferralPathwayId(UUID.fromString("ca4d147f-cbc3-4c2d-9e33-627c9974d802"))
        .create(),
    )

    entityManager.clear()

    val nonContactSessions = referralNonContactSessionRepository.findAllByReferralIdAndDeletedAtIsNullOrderByCreatedAtDesc(referral.id)
    assertEquals(2, nonContactSessions.size)

    // Check that the non ontact sessions are returned in the opposite order they were added
    val earlierSession = nonContactSessions[0]
    assertEquals(secondReferralNonContactSession.id, earlierSession.id)
    assertEquals("Second title", earlierSession.title)
    assertEquals("Second details", earlierSession.details)

    val laterSession = nonContactSessions[1]
    assertEquals(firstReferralNonContactSession.id, laterSession.id)
    assertEquals("First title", laterSession.title)
    assertEquals("First details", laterSession.details)
  }

  @Test
  @Transactional
  fun `should not return soft deleted sessions`() {
    val referral = referralHelper.createReferral()

    val referralNonContactSession = referralNonContactSessionRepository.saveAndFlush(
      ReferralNonContactSessionFactory()
        .withReferralId(referral.id)
        .withStartDateTime(OffsetDateTime.of(2026, 11, 5, 13, 30, 0, 0, ZoneOffset.UTC))
        .withDurationHours(0)
        .withDurationMinutes(10)
        .withTitle("Test title")
        .withDetails("Test details")
        .withDeletedAt(OffsetDateTime.now())
        .create(),
    )

    referralNonContactSessionReferenceDataReferralPathwayRepository.saveAndFlush(
      ReferralNonContactSessionReferenceDataReferralPathwayFactory()
        .withReferralNonContactSessionId(referralNonContactSession.id)
        .withReferenceDataReferralPathwayId(UUID.fromString("ca4d147f-cbc3-4c2d-9e33-627c9974d802"))
        .create(),
    )

    entityManager.clear()

    val nonContactSessions = referralNonContactSessionRepository.findAllByReferralIdAndDeletedAtIsNullOrderByCreatedAtDesc(referral.id)
    assertEquals(0, nonContactSessions.size)
  }
}
