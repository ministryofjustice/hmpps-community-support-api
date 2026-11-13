package uk.gov.justice.digital.hmpps.communitysupportapi.controller

import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod.POST
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.reactive.server.expectBody
import uk.gov.justice.digital.hmpps.communitysupportapi.authorization.UserMapper
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.SubmitCaseNoteRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.SuccessMessageResponse
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralUser
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.ReferralTestSupport
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferenceDataReferralPathwayRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralNotesReferenceDataReferralPathwayRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralNotesRepository
import uk.gov.justice.hmpps.kotlin.auth.HmppsAuthenticationHolder
import java.util.UUID

class CaseNoteControllerIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var referralHelper: ReferralTestSupport

  @Autowired
  private lateinit var caseNoteRepository: ReferralNotesRepository

  @Autowired
  private lateinit var referralNotesReferenceDataReferralPathwayRepository: ReferralNotesReferenceDataReferralPathwayRepository

  @Autowired
  private lateinit var referenceDataReferralPathwayRepository: ReferenceDataReferralPathwayRepository

  @MockitoBean
  private lateinit var userMapper: UserMapper

  private lateinit var testUser: ReferralUser

  @Nested
  @DisplayName("POST /referral/{referralReference}/case-note")
  inner class PostCaseNote {
    @BeforeEach
    fun setup() {
      testUser = referralHelper.ensureReferralUser()

//      referenceDataReferralPathwayRepository.saveAll(
//        listOf(
//          ReferenceDataReferralPathway(
//            id = UUID.fromString("ca4d147f-cbc3-4c2d-9e33-627c9974d801"),
//            title = "Community Links",
//            displayOrder = 1,
//          ),
//          ReferenceDataReferralPathway(
//            id = UUID.fromString("ca4d147f-cbc3-4c2d-9e33-627c9974d802"),
//            title = "Housing Support",
//            displayOrder = 2,
//          ),
//          ReferenceDataReferralPathway(
//            id = UUID.fromString("ca4d147f-cbc3-4c2d-9e33-627c9974d803"),
//            title = "Jobs and skills support",
//            displayOrder = 3,
//          ),
//          ReferenceDataReferralPathway(
//            id = UUID.fromString("ca4d147f-cbc3-4c2d-9e33-627c9974d804"),
//            title = "Money management",
//            displayOrder = 4,
//          ),
//        ),
//      )
    }

    @Test
    fun `should return unauthorized if no token`() {
      assertUnauthorized(
        POST,
        "/referral/AB1234CD/case-note",
      )
    }

    @Test
    fun `should return forbidden if no role`() {
      assertForbiddenNoRole(
        POST,
        "/referral/AB1234CD/case-note",
        SubmitCaseNoteRequest(
          title = "test",
          details = "test",
        ),
      )
    }

    @Test
    fun `should return forbidden if wrong role`() {
      assertForbiddenWrongRole(
        POST,
        "/referral/AB1234CD/case-note",
        SubmitCaseNoteRequest(
          title = "test",
          details = "test",
        ),
      )
    }

    @Test
    fun `should return OK with success message for a successful case note submission`() {
      whenever(userMapper.fromToken(any<HmppsAuthenticationHolder>())).thenReturn(testUser)
      val referral = referralHelper.createReferral()

      webTestClient.post()
        .uri("/referral/${referral.referenceNumber}/case-note")
        .headers(setAuthorisation())
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(
          SubmitCaseNoteRequest(
            title = "Test note title",
            details = "Test note details",
            shouldNotify = true,
            pathways = listOf(UUID.fromString("ca4d147f-cbc3-4c2d-9e33-627c9974d802"), UUID.fromString("ca4d147f-cbc3-4c2d-9e33-627c9974d803")),
          ),
        )
        .exchange()
        .expectStatus().isOk
        .expectBody<SuccessMessageResponse>()
        .consumeWith { response ->
          val body = response.responseBody!!
          body.success shouldBe true
          body.message shouldBe "Case note submitted successfully"
        }

      val caseNotes = caseNoteRepository.findAllByReferralIdAndDeletedAtIsNullOrderByCreatedAtDesc(referral.id)
      caseNotes.size shouldBe 1
      val note = caseNotes.first()
      note.title shouldBe "Test note title"
      note.details shouldBe "Test note details"
      note.shouldNotify.shouldBeTrue()

      val caseNotePathways = referralNotesReferenceDataReferralPathwayRepository.findAllByReferralNoteId(note.id)
      caseNotePathways.size shouldBe 2
      caseNotePathways.map { pathway -> pathway.referenceDataReferralPathwayId }
        .shouldContainExactlyInAnyOrder(
          listOf(
            UUID.fromString("ca4d147f-cbc3-4c2d-9e33-627c9974d802"),
            UUID.fromString("ca4d147f-cbc3-4c2d-9e33-627c9974d803"),
          ),
        )
    }

    @Test
    fun `should return 400 when not supplying a pathway`() {
      whenever(userMapper.fromToken(any<HmppsAuthenticationHolder>())).thenReturn(testUser)
      val referral = referralHelper.createReferral()

      webTestClient.post()
        .uri("/referral/${referral.referenceNumber}/case-note")
        .headers(setAuthorisation())
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(
          SubmitCaseNoteRequest(
            title = "Test note title",
            details = "Test note details",
            shouldNotify = true,
          ),
        )
        .exchange()
        .expectStatus().is4xxClientError
        .expectStatus().isEqualTo(400)
    }

    @Test
    fun `should return 400 when supplying an invalid pathway`() {
      whenever(userMapper.fromToken(any<HmppsAuthenticationHolder>())).thenReturn(testUser)
      val referral = referralHelper.createReferral()

      webTestClient.post()
        .uri("/referral/${referral.referenceNumber}/case-note")
        .headers(setAuthorisation())
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(
          SubmitCaseNoteRequest(
            title = "Test note title",
            details = "Test note details",
            shouldNotify = true,
            pathways = listOf(UUID.fromString("12345678-1234-1234-1234-1234567890ab")),
          ),
        )
        .exchange()
        .expectStatus().is4xxClientError
        .expectStatus().isEqualTo(400)
    }
  }
}
