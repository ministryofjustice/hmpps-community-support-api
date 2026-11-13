package uk.gov.justice.digital.hmpps.communitysupportapi.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.SubmitCaseNoteRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.ReferralTestSupport
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralNotesReferenceDataReferralPathwayRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralNotesRepository
import java.util.UUID

class CaseNoteServiceIntegrationTest :
  IntegrationTestBase(),
  AfterAllCallback {
  @Autowired
  lateinit var caseNoteService: CaseNoteService

  @Autowired
  lateinit var referralHelper: ReferralTestSupport

  @Autowired
  lateinit var referralNotesRepository: ReferralNotesRepository

  @Autowired
  lateinit var referralNotesReferenceDataReferralPathwayRepository: ReferralNotesReferenceDataReferralPathwayRepository

  override fun afterAll(context: ExtensionContext) {
    testDataCleaner.cleanAllTables()
  }

  @Test
  fun `should create a new case note on request`() {
    // Given
    val user = referralHelper.ensureReferralUser()
    val referral = referralHelper.createReferral(submittedBy = user)

    // When
    val response = caseNoteService.submitCaseNote(
      referral.referenceNumber!!,
      SubmitCaseNoteRequest(
        title = "Test note title",
        details = "Test note details",
        shouldNotify = true,
        pathways = listOf(UUID.fromString("ca4d147f-cbc3-4c2d-9e33-627c9974d802")),
      ),
      user.fullName,
    )

    // Then
    assertEquals("Case note submitted successfully", response.message)
    assertTrue(response.success)

    val notes = referralNotesRepository.findAllByReferralIdAndDeletedAtIsNullOrderByCreatedAtDesc(referral.id)
    assertEquals(notes.size, 1)
    assertEquals(notes.first().title, "Test note title")
    assertEquals(notes.first().details, "Test note details")
    assertTrue(notes.first().shouldNotify)

    val pathways = referralNotesReferenceDataReferralPathwayRepository.findAllByReferralNoteId(notes.first().id)
    assertEquals(pathways.size, 1)
    assertEquals(pathways.first().referenceDataReferralPathwayId, UUID.fromString("ca4d147f-cbc3-4c2d-9e33-627c9974d802"))
  }
}
