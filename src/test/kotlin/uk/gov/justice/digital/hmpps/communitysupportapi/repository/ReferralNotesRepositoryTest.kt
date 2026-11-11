package uk.gov.justice.digital.hmpps.communitysupportapi.repository

import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.ReferralTestSupport
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.ReferralNotesFactory
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.ReferralNotesReferenceDataReferralPathwayFactory
import java.time.OffsetDateTime
import java.util.UUID

class ReferralNotesRepositoryTest : IntegrationTestBase() {

  @Autowired
  private lateinit var referralNotesRepository: ReferralNotesRepository

  @Autowired
  private lateinit var referralNotesReferenceDataReferralPathwayRepository: ReferralNotesReferenceDataReferralPathwayRepository

  @Autowired
  private lateinit var referralHelper: ReferralTestSupport

  @Autowired
  private lateinit var entityManager: EntityManager

  @Test
  @Transactional
  fun `should load referral notes with correct pathways`() {
    val referral = referralHelper.createReferral()

    val referralNote = referralNotesRepository.saveAndFlush(
      ReferralNotesFactory()
        .withReferralId(referral.id)
        .withTitle("Test title")
        .withDetails("Test details")
        .create(),
    )

    referralNotesReferenceDataReferralPathwayRepository.saveAndFlush(
      ReferralNotesReferenceDataReferralPathwayFactory()
        .withReferralNoteId(referralNote.id)
        .withReferenceDataReferralPathwayId(UUID.fromString("ca4d147f-cbc3-4c2d-9e33-627c9974d802"))
        .create(),
    )

    referralNotesReferenceDataReferralPathwayRepository.saveAndFlush(
      ReferralNotesReferenceDataReferralPathwayFactory()
        .withReferralNoteId(referralNote.id)
        .withReferenceDataReferralPathwayId(UUID.fromString("ca4d147f-cbc3-4c2d-9e33-627c9974d803"))
        .create(),
    )

    entityManager.clear()

    val notes = referralNotesRepository.findAllByReferralIdAndDeletedAtIsNullOrderByCreatedAtDesc(referral.id)
    assertEquals(1, notes.size)

    val note = notes.first()
    assertEquals(referral.id, note.referralId)
    assertEquals("Test title", note.title)
    assertEquals("Test details", note.details)
    assertFalse(note.shouldNotify)

    val pathways = note.referralPathways
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
  fun `should load referral notes in order of createdAt time descending`() {
    val referral = referralHelper.createReferral()

    val firstReferralNote = referralNotesRepository.saveAndFlush(
      ReferralNotesFactory()
        .withReferralId(referral.id)
        .withTitle("First note")
        .withDetails("First note details")
        .withShouldNotify(true)
        .create(),
    )

    val secondReferralNote = referralNotesRepository.saveAndFlush(
      ReferralNotesFactory()
        .withReferralId(referral.id)
        .withTitle("Second note")
        .withDetails("Second note details")
        .create(),
    )

    referralNotesReferenceDataReferralPathwayRepository.saveAndFlush(
      ReferralNotesReferenceDataReferralPathwayFactory()
        .withReferralNoteId(firstReferralNote.id)
        .withReferenceDataReferralPathwayId(UUID.fromString("ca4d147f-cbc3-4c2d-9e33-627c9974d802"))
        .create(),
    )

    referralNotesReferenceDataReferralPathwayRepository.saveAndFlush(
      ReferralNotesReferenceDataReferralPathwayFactory()
        .withReferralNoteId(secondReferralNote.id)
        .withReferenceDataReferralPathwayId(UUID.fromString("ca4d147f-cbc3-4c2d-9e33-627c9974d802"))
        .create(),
    )

    entityManager.clear()

    val notes = referralNotesRepository.findAllByReferralIdAndDeletedAtIsNullOrderByCreatedAtDesc(referral.id)
    assertEquals(2, notes.size)

    // Check that the notes are returned in the opposite order they were added
    val earlierNote = notes[0]
    assertEquals(secondReferralNote.id, earlierNote.id)
    assertEquals("Second note", earlierNote.title)
    assertEquals("Second note details", earlierNote.details)
    assertFalse(earlierNote.shouldNotify)

    val laterNote = notes[1]
    assertEquals(firstReferralNote.id, laterNote.id)
    assertEquals("First note", laterNote.title)
    assertEquals("First note details", laterNote.details)
    assertTrue(laterNote.shouldNotify)
  }

  @Test
  @Transactional
  fun `should not return soft deleted notes`() {
    val referral = referralHelper.createReferral()

    val referralNote = referralNotesRepository.saveAndFlush(
      ReferralNotesFactory()
        .withReferralId(referral.id)
        .withTitle("Test title")
        .withDetails("Test details")
        .withDeletedAt(OffsetDateTime.now())
        .create(),
    )

    referralNotesReferenceDataReferralPathwayRepository.saveAndFlush(
      ReferralNotesReferenceDataReferralPathwayFactory()
        .withReferralNoteId(referralNote.id)
        .withReferenceDataReferralPathwayId(UUID.fromString("ca4d147f-cbc3-4c2d-9e33-627c9974d802"))
        .create(),
    )

    entityManager.clear()

    val notes = referralNotesRepository.findAllByReferralIdAndDeletedAtIsNullOrderByCreatedAtDesc(referral.id)
    assertEquals(0, notes.size)
  }
}
