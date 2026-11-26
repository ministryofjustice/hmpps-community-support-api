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
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpMethod.POST
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.reactive.server.expectBody
import uk.gov.justice.digital.hmpps.communitysupportapi.authorization.UserMapper
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.PageResponse
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.ReferenceDataReferralPathwayDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.ReferenceDataReferralPathwaysDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.ReferralNoteOrNonContactSessionBffDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.SubmitCaseNoteRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.SubmitNonContactSessionRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.SuccessMessageResponse
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralNonContactSession
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralNote
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralUser
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.ReferralTestSupport
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferenceDataReferralPathwayRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralNonContactSessionReferenceDataReferralPathwayRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralNonContactSessionRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralNotesReferenceDataReferralPathwayRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralNotesRepository
import uk.gov.justice.hmpps.kotlin.auth.HmppsAuthenticationHolder
import java.time.OffsetDateTime
import java.time.ZoneOffset
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

  @Autowired
  private lateinit var referralNonContactSessionRepository: ReferralNonContactSessionRepository

  @Autowired
  private lateinit var referralNonContactSessionReferenceDataReferralPathwayRepository: ReferralNonContactSessionReferenceDataReferralPathwayRepository

  @MockitoBean
  private lateinit var userMapper: UserMapper

  private lateinit var testUser: ReferralUser

  @Nested
  @DisplayName("POST /referral/{referralReference}/case-note")
  inner class PostCaseNote {
    @BeforeEach
    fun setup() {
      testUser = referralHelper.ensureReferralUser()
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

      val emptyCaseNotes = caseNoteRepository.findAllByReferralIdAndDeletedAtIsNullOrderByCreatedAtDesc(referral.id)
      emptyCaseNotes.size shouldBe 0

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
    fun `should return OK with success message when not supplying a pathway`() {
      whenever(userMapper.fromToken(any<HmppsAuthenticationHolder>())).thenReturn(testUser)
      val referral = referralHelper.createReferral()

      val emptyCaseNotes = caseNoteRepository.findAllByReferralIdAndDeletedAtIsNullOrderByCreatedAtDesc(referral.id)
      emptyCaseNotes.size shouldBe 0

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
      caseNotePathways.size shouldBe 0
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

  @Nested
  @DisplayName("POST /referral/{referralReference}/non-contact-session")
  inner class PostNonContactSession {
    @BeforeEach
    fun setup() {
      testUser = referralHelper.ensureReferralUser()
    }

    @Test
    fun `should return unauthorized if no token`() {
      assertUnauthorized(
        POST,
        "/referral/AB1234CD/non-contact-session",
      )
    }

    @Test
    fun `should return forbidden if no role`() {
      assertForbiddenNoRole(
        POST,
        "/referral/AB1234CD/non-contact-session",
        SubmitNonContactSessionRequest(
          title = "test",
          details = "test",
          startDate = OffsetDateTime.now().plusDays(1),
          durationHours = 1,
          durationMinutes = 30,
          pathways = listOf(UUID.fromString("ca4d147f-cbc3-4c2d-9e33-627c9974d802")),
        ),
      )
    }

    @Test
    fun `should return forbidden if wrong role`() {
      assertForbiddenWrongRole(
        POST,
        "/referral/AB1234CD/non-contact-session",
        SubmitNonContactSessionRequest(
          title = "test",
          details = "test",
          startDate = OffsetDateTime.now().plusDays(1),
          durationHours = 1,
          durationMinutes = 30,
          pathways = listOf(UUID.fromString("ca4d147f-cbc3-4c2d-9e33-627c9974d802")),
        ),
      )
    }

    @Test
    fun `should return OK with success message for a successful non contact session submission`() {
      whenever(userMapper.fromToken(any<HmppsAuthenticationHolder>())).thenReturn(testUser)
      val referral = referralHelper.createReferral()

      val emptySessions = referralNonContactSessionRepository.findAllByReferralIdAndDeletedAtIsNullOrderByCreatedAtDesc(referral.id)
      emptySessions.size shouldBe 0

      webTestClient.post()
        .uri("/referral/${referral.referenceNumber}/non-contact-session")
        .headers(setAuthorisation())
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(
          SubmitNonContactSessionRequest(
            title = "Test session title",
            details = "Test session details",
            startDate = OffsetDateTime.now().plusDays(1),
            durationHours = 1,
            durationMinutes = 30,
            pathways = listOf(UUID.fromString("ca4d147f-cbc3-4c2d-9e33-627c9974d802"), UUID.fromString("ca4d147f-cbc3-4c2d-9e33-627c9974d803")),
          ),
        )
        .exchange()
        .expectStatus().isOk
        .expectBody<SuccessMessageResponse>()
        .consumeWith { response ->
          val body = response.responseBody!!
          body.success shouldBe true
          body.message shouldBe "Non contact session submitted successfully"
        }

      val emptyCaseNotes = caseNoteRepository.findAllByReferralIdAndDeletedAtIsNullOrderByCreatedAtDesc(referral.id)
      emptyCaseNotes.size shouldBe 0

      val sessions = referralNonContactSessionRepository.findAllByReferralIdAndDeletedAtIsNullOrderByCreatedAtDesc(referral.id)
      sessions.size shouldBe 1

      val session = sessions.first()
      session.title shouldBe "Test session title"
      session.details shouldBe "Test session details"

      val nonContactSessionPathways = referralNonContactSessionReferenceDataReferralPathwayRepository.findAllByReferralNonContactSessionId(session.id)
      nonContactSessionPathways.size shouldBe 2
      nonContactSessionPathways.map { pathway -> pathway.referenceDataReferralPathwayId }
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
        .uri("/referral/${referral.referenceNumber}/non-contact-session")
        .headers(setAuthorisation())
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(
          SubmitNonContactSessionRequest(
            title = "Test note title",
            details = "Test note details",
            startDate = OffsetDateTime.now().plusDays(1),
            durationHours = 1,
            durationMinutes = 30,
            pathways = emptyList(),
          ),
        )
        .exchange()
        .expectStatus().isEqualTo(400)
        .expectBody()
        .jsonPath("$.status").isEqualTo("400")
        .jsonPath("$.userMessage").isEqualTo("Validation failure: pathways: The session must have at least one pathway")
    }

    @Test
    fun `should return 400 when supplying an invalid pathway`() {
      whenever(userMapper.fromToken(any<HmppsAuthenticationHolder>())).thenReturn(testUser)
      val referral = referralHelper.createReferral()

      webTestClient.post()
        .uri("/referral/${referral.referenceNumber}/non-contact-session")
        .headers(setAuthorisation())
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(
          SubmitNonContactSessionRequest(
            title = "Test note title",
            details = "Test note details",
            startDate = OffsetDateTime.now().plusDays(1),
            durationHours = 1,
            durationMinutes = 30,
            pathways = listOf(UUID.fromString("12345678-1234-1234-1234-1234567890ab")),
          ),
        )
        .exchange()
        .expectStatus().is4xxClientError
        .expectStatus().isEqualTo(400)
        .expectBody()
        .jsonPath("$.userMessage").isEqualTo("Validation failure: One of [12345678-1234-1234-1234-1234567890ab] is not a valid pathway ID")
    }
  }

  @Nested
  @DisplayName("GET /bff/referral/{referralReference}/notes")
  inner class GetNotesAndNonContactSessions {
    private val baseTime: OffsetDateTime = OffsetDateTime.of(2026, 1, 5, 19, 2, 0, 0, ZoneOffset.UTC)

    @Test
    fun `should return unauthorized if no token`() {
      assertUnauthorized(GET, "/bff/referral/AB1234CD/notes")
    }

    @Test
    fun `should return forbidden if no role`() {
      assertForbiddenNoRole(GET, "/bff/referral/AB1234CD/notes")
    }

    @Test
    fun `should return forbidden if wrong role`() {
      assertForbiddenWrongRole(GET, "/bff/referral/AB1234CD/notes")
    }

    @Test
    fun `should return not found for an unknown referral`() {
      assertNotFound(GET, "/bff/referral/ZZ9999ZZ/notes")
    }

    @Test
    fun `should return notes and non-contact sessions merged newest first`() {
      val referral = referralHelper.createReferral()
      caseNoteRepository.save(
        ReferralNote(
          id = UUID.randomUUID(),
          referralId = referral.id,
          createdAt = baseTime,
          createdBy = "Note author",
          title = "Note title",
          details = "Note details",
        ),
      )
      referralNonContactSessionRepository.save(
        ReferralNonContactSession(
          id = UUID.randomUUID(),
          referralId = referral.id,
          createdAt = baseTime.plusDays(1).plusHours(15),
          createdBy = "Session author",
          title = "Session title",
          details = "Session details",
          startDateTime = baseTime.plusDays(1),
          durationHours = 1,
          durationMinutes = 0,
        ),
      )

      webTestClient.get()
        .uri("/bff/referral/${referral.referenceNumber}/notes")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus().isOk
        .expectBody<PageResponse<ReferralNoteOrNonContactSessionBffDto>>()
        .consumeWith { response ->
          val body = response.responseBody!!
          body.page shouldBe 0
          body.size shouldBe 25
          body.totalElements shouldBe 2
          body.totalPages shouldBe 1
          body.content.map { it.title } shouldBe listOf("Session title", "Note title")
          body.content[0].createdAt shouldBe "Wed 7 Jan 2026 10:02am"
          body.content[0].createdBy shouldBe "Session author"
          body.content[1].createdAt shouldBe "Mon 5 Jan 2026 7:02pm"
          body.content[1].details shouldBe "Note details"
          body.content[1].detailsTruncated shouldBe false
        }
    }

    @Test
    fun `should truncate details to 40 words and flag them as truncated`() {
      val referral = referralHelper.createReferral()
      val longDetails = (1..60).joinToString(" ") { "word$it" }
      caseNoteRepository.save(
        ReferralNote(
          id = UUID.randomUUID(),
          referralId = referral.id,
          createdAt = baseTime,
          createdBy = "Note author",
          title = "Long note",
          details = longDetails,
        ),
      )

      webTestClient.get()
        .uri("/bff/referral/${referral.referenceNumber}/notes")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus().isOk
        .expectBody<PageResponse<ReferralNoteOrNonContactSessionBffDto>>()
        .consumeWith { response ->
          val item = response.responseBody!!.content.single()
          item.details shouldBe (1..40).joinToString(" ") { "word$it" }
          item.detailsTruncated shouldBe true
        }
    }

    @Test
    fun `should page results with a default page size of 25`() {
      val referral = referralHelper.createReferral()
      (1..26).forEach { i ->
        caseNoteRepository.save(
          ReferralNote(
            id = UUID.randomUUID(),
            referralId = referral.id,
            createdAt = baseTime.plusMinutes(i.toLong()),
            createdBy = "Note author",
            title = "Note $i",
            details = "Details $i",
          ),
        )
      }

      webTestClient.get()
        .uri("/bff/referral/${referral.referenceNumber}/notes")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus().isOk
        .expectBody<PageResponse<ReferralNoteOrNonContactSessionBffDto>>()
        .consumeWith { response ->
          val body = response.responseBody!!
          body.content.size shouldBe 25
          body.totalElements shouldBe 26
          body.totalPages shouldBe 2
          body.content.first().title shouldBe "Note 26"
        }

      webTestClient.get()
        .uri("/bff/referral/${referral.referenceNumber}/notes?page=1")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus().isOk
        .expectBody<PageResponse<ReferralNoteOrNonContactSessionBffDto>>()
        .consumeWith { response ->
          val body = response.responseBody!!
          body.page shouldBe 1
          body.content.map { it.title } shouldBe listOf("Note 1")
        }
    }

    @Test
    fun `should exclude soft-deleted notes and non-contact sessions from content and totalElements`() {
      val referral = referralHelper.createReferral()
      caseNoteRepository.save(
        ReferralNote(
          id = UUID.randomUUID(),
          referralId = referral.id,
          createdAt = baseTime,
          createdBy = "Note author",
          title = "Active note",
          details = "Active details",
        ),
      )
      caseNoteRepository.save(
        ReferralNote(
          id = UUID.randomUUID(),
          referralId = referral.id,
          createdAt = baseTime.plusHours(1),
          createdBy = "Note author",
          title = "Deleted note",
          details = "Deleted details",
          deletedAt = baseTime.plusDays(1),
        ),
      )
      referralNonContactSessionRepository.save(
        ReferralNonContactSession(
          id = UUID.randomUUID(),
          referralId = referral.id,
          createdAt = baseTime.plusHours(2),
          createdBy = "Session author",
          title = "Deleted session",
          details = "Deleted details",
          startDateTime = baseTime,
          durationHours = 1,
          durationMinutes = 0,
          deletedAt = baseTime.plusDays(1),
        ),
      )

      webTestClient.get()
        .uri("/bff/referral/${referral.referenceNumber}/notes")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus().isOk
        .expectBody<PageResponse<ReferralNoteOrNonContactSessionBffDto>>()
        .consumeWith { response ->
          val body = response.responseBody!!
          body.totalElements shouldBe 1
          body.totalPages shouldBe 1
          body.content.map { it.title } shouldBe listOf("Active note")
        }
    }

    @Test
    fun `should only return notes and non-contact sessions belonging to the requested referral`() {
      val referral = referralHelper.createReferral()
      val otherPerson = referralHelper.createPerson(identifier = "X654321")
      val otherReferral = referralHelper.createReferral(
        person = otherPerson,
        referenceNumber = "CD5678EF",
        submittedBy = referralHelper.ensureReferralUser(),
      )
      caseNoteRepository.save(
        ReferralNote(
          id = UUID.randomUUID(),
          referralId = referral.id,
          createdAt = baseTime,
          createdBy = "Note author",
          title = "Own note",
          details = "Own details",
        ),
      )
      caseNoteRepository.save(
        ReferralNote(
          id = UUID.randomUUID(),
          referralId = otherReferral.id,
          createdAt = baseTime.plusHours(1),
          createdBy = "Note author",
          title = "Other referral note",
          details = "Other details",
        ),
      )
      referralNonContactSessionRepository.save(
        ReferralNonContactSession(
          id = UUID.randomUUID(),
          referralId = otherReferral.id,
          createdAt = baseTime.plusHours(2),
          createdBy = "Session author",
          title = "Other referral session",
          details = "Other details",
          startDateTime = baseTime,
          durationHours = 1,
          durationMinutes = 0,
        ),
      )

      webTestClient.get()
        .uri("/bff/referral/${referral.referenceNumber}/notes")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus().isOk
        .expectBody<PageResponse<ReferralNoteOrNonContactSessionBffDto>>()
        .consumeWith { response ->
          val body = response.responseBody!!
          body.totalElements shouldBe 1
          body.content.map { it.title } shouldBe listOf("Own note")
        }
    }
  }

  @Test
  fun `should return 200 when requesting pathways`() {
    testUser = referralHelper.ensureReferralUser()
    whenever(userMapper.fromToken(any<HmppsAuthenticationHolder>())).thenReturn(testUser)
    val referral = referralHelper.createReferral()

    webTestClient.get()
      .uri("/bff/referral/${referral.referenceNumber}/notes/pathways")
      .headers(setAuthorisation())
      .exchange()
      .expectStatus().isOk
      .expectBody<ReferenceDataReferralPathwaysDto>()
      .consumeWith { response ->
        response.responseBody shouldBe ReferenceDataReferralPathwaysDto(
          listOf(
            ReferenceDataReferralPathwayDto(UUID.fromString("ca4d147f-cbc3-4c2d-9e33-627c9974d801"), "Community links"),
            ReferenceDataReferralPathwayDto(UUID.fromString("ca4d147f-cbc3-4c2d-9e33-627c9974d802"), "Housing support"),
            ReferenceDataReferralPathwayDto(
              UUID.fromString("ca4d147f-cbc3-4c2d-9e33-627c9974d803"),
              "Jobs and skills support",
            ),
            ReferenceDataReferralPathwayDto(
              UUID.fromString("ca4d147f-cbc3-4c2d-9e33-627c9974d804"),
              "Money management",
            ),
          ),
        )
      }
  }
}
