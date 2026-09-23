package uk.gov.justice.digital.hmpps.communitysupportapi.controller

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpMethod.PATCH
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.reactive.server.expectBody
import uk.gov.justice.digital.hmpps.communitysupportapi.authorization.UserMapper
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.ActionPlanActionRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.ActionPlanActionResponse
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.ActionPlanActivityRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.ActionPlanSelectANeedResponse
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.ActionPlanSessionDeliveryDetailsRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.ActionPlanSessionDeliveryDetailsResponse
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.ActionPlanSummaryDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.SessionDeliveryDetailsQuestionAnswer
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.SessionDeliveryDetailsQuestionAnswers
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanQuestionAnswerType
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanQuestionType
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanStepQuestionAnswerDetails
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanStepQuestionAnswerHeader
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanStepType
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Referral
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralUser
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.ActionPlanTestSupport
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.ReferralTestSupport
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ActionPlanActivityRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ActionPlanRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ActionPlanStepQuestionAnswerDetailsRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ActionPlanStepQuestionAnswerHeaderRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ActionPlanStepQuestionChoiceRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ActionPlanStepQuestionRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ActionPlanStepRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ActionPlanTemplateRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.NeedRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.ActionPlanStepFactory
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.ActionPlanStepQuestionChoiceFactory
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.ActionPlanStepQuestionFactory
import uk.gov.justice.digital.hmpps.communitysupportapi.util.ReferralReferenceTestUtil.randomReferralReference
import uk.gov.justice.hmpps.kotlin.auth.HmppsAuthenticationHolder
import java.time.OffsetDateTime
import java.util.UUID

class ActionPlanControllerIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var referralHelper: ReferralTestSupport

  @Autowired
  private lateinit var needRepository: NeedRepository

  @Autowired
  private lateinit var actionPlanHelper: ActionPlanTestSupport

  @Autowired
  private lateinit var actionPlanStepRepository: ActionPlanStepRepository

  @Autowired
  private lateinit var actionPlanStepQuestionRepository: ActionPlanStepQuestionRepository

  @Autowired
  private lateinit var actionPlanStepQuestionChoiceRepository: ActionPlanStepQuestionChoiceRepository

  @Autowired
  private lateinit var actionPlanStepQuestionAnswerHeaderRepository: ActionPlanStepQuestionAnswerHeaderRepository

  @Autowired
  private lateinit var actionPlanStepQuestionAnswerDetailsRepository: ActionPlanStepQuestionAnswerDetailsRepository

  @Autowired
  private lateinit var actionPlanActivityRepository: ActionPlanActivityRepository

  @Autowired
  private lateinit var actionPlanRepository: ActionPlanRepository

  @Autowired
  private lateinit var actionPlanTemplateRepository: ActionPlanTemplateRepository

  @MockitoBean
  private lateinit var userMapper: UserMapper

  private lateinit var testUser: ReferralUser

  @Nested
  @DisplayName("GET /bff/referral/{referralReference}/action-plan")
  inner class GetActionPlanSummaryEndpoint {
    @BeforeEach
    fun setup() {
      testUser = referralHelper.ensureReferralUser()
    }

    @Test
    fun `should return OK with action plan summary for a valid referral reference`() {
      val user = referralHelper.ensureReferralUser()
      val person = referralHelper.createPerson(firstName = "Adam", lastName = "Smith")
      val referral =
        referralHelper.createReferral(person = person, referenceNumber = randomReferralReference(), submittedBy = user)

      // Setup action plan
      val expectedNeed = needRepository.findAllByOrderByOrderNumberAsc().first()

      val actionPlanTemplate = actionPlanHelper.createActionPlanTemplate()
      val actionPlan = actionPlanHelper.createActionPlan(referral.id, actionPlanTemplate.id)
      val actionPlanStep = actionPlanHelper.createActionPlanStep(actionPlanTemplate.id)
      val actionPlanStepQuestion = actionPlanHelper.createActionPlanStepQuestion(actionPlanStep.id, needId = expectedNeed.id)
      val actionPlanStepQuestionAnswerHeader = actionPlanHelper.createActionPlanStepQuestionAnswerHeader(actionPlan.id, actionPlanStepQuestion.id)
      val actionPlanStepQuestionAnswerDetails = actionPlanHelper.createActionPlanStepQuestionAnswerDetails(actionPlanStepQuestionAnswerHeader.id)
      val actionPlanActivity = actionPlanHelper.createActionPlanActivity(actionPlanStepQuestionAnswerHeader.id)

      webTestClient.get()
        .uri("/bff/referral/${referral.referenceNumber}/action-plan")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus().isOk
        .expectBody<ActionPlanSummaryDto>()
        .consumeWith { response ->
          val body = response.responseBody!!
          body.personDetails.firstName shouldBe "Adam"
          body.personDetails.lastName shouldBe "Smith"
          body.needs.size shouldBe 1
          val need = body.needs.first()
          need.label shouldBe expectedNeed.label
          need.id shouldBe expectedNeed.id
          need.outcomes.size shouldBe 1
          val outcome = need.outcomes.first()
          outcome.label shouldBe actionPlanStepQuestionAnswerDetails.content
          outcome.id shouldBe actionPlanStepQuestionAnswerHeader.id
          outcome.activities.size shouldBe 1
          val activity = outcome.activities.first()
          activity.who shouldBe actionPlanActivity.who
          activity.details shouldBe actionPlanActivity.activityDetails
          activity.id shouldBe actionPlanActivity.id
        }
    }

    @Nested
    @DisplayName("GET /bff/referral/action-plan/select-a-need")
    inner class GetSelectANeedEndpoint {
      @Test
      fun `should return unauthorized if no token`() {
        assertUnauthorized(GET, "/bff/referral/action-plan/select-a-need")
      }

      @Test
      fun `should return forbidden if no role`() {
        assertForbiddenNoRole(GET, "/bff/referral/action-plan/select-a-need")
      }

      @Test
      fun `should return forbidden if wrong role`() {
        assertForbiddenWrongRole(GET, "/bff/referral/action-plan/select-a-need")
      }

      @Test
      fun `should return needs with related outcomes ordered by need and outcome order`() {
        val expectedNeeds = needRepository.findAllByOrderByOrderNumberAsc()

        webTestClient.get()
          .uri("/bff/referral/action-plan/select-a-need")
          .headers(setAuthorisation())
          .exchange()
          .expectStatus().isOk
          .expectBody<ActionPlanSelectANeedResponse>()
          .consumeWith { response ->
            val body = response.responseBody!!

            body.needs.map { it.id } shouldBe expectedNeeds.map { it.id }
            body.needs.map { it.label } shouldBe expectedNeeds.map { it.label }
            body.needs[0].outcomes.map { it.id } shouldBe listOf(
              UUID.fromString("f2a3c4d5-e6f7-4801-9001-000000000001"),
              UUID.fromString("f2a3c4d5-e6f7-4801-9001-000000000002"),
            )
            body.needs[0].outcomes.map { it.text } shouldBe listOf(
              "I want to secure and maintain settled and suitable accommodation.",
              "I want to manage my tenancy and prevent rent arrears or other debts while I am in custody.",
            )
            body.needs[1].outcomes.map { it.id } shouldBe listOf(
              UUID.fromString("f2a3c4d5-e6f7-4801-9001-000000000003"),
            )
            body.needs[1].outcomes.map { it.text } shouldBe listOf(
              "I want to find and keep suitable employment, or take steps towards employment through education, training, or other opportunities.",
            )
          }
      }
    }

    @Nested
    @DisplayName("GET /bff/referral/{referralReference}/action-plan/session-delivery-details")
    inner class GetSessionDeliveryDetailsEndpoint {
      @Test
      fun `should return unauthorized if no token`() {
        assertUnauthorized(GET, "/bff/referral/AB1234CD/action-plan/session-delivery-details")
      }

      @Test
      fun `should return forbidden if no role`() {
        assertForbiddenNoRole(GET, "/bff/referral/AB1234CD/action-plan/session-delivery-details")
      }

      @Test
      fun `should return forbidden if wrong role`() {
        assertForbiddenWrongRole(GET, "/bff/referral/AB1234CD/action-plan/session-delivery-details")
      }

      @Test
      fun `should return session delivery questions with choices ordered by display order`() {
        val referral = createReferral("Jane", "Doe")
        val actionPlanTemplate = actionPlanHelper.createActionPlanTemplate()
        actionPlanHelper.createActionPlan(referralId = referral.id, templateId = actionPlanTemplate.id)

        val sessionDeliveryStep = actionPlanStepRepository.save(
          ActionPlanStepFactory()
            .withActionPlanTemplateId(actionPlanTemplate.id)
            .withOrderNumber(2)
            .withName("Service Delivery Details")
            .withStepType(ActionPlanStepType.SESSION_DELIVERY)
            .create(),
        )

        val question1 = actionPlanStepQuestionRepository.save(
          ActionPlanStepQuestionFactory()
            .withActionPlanStepId(sessionDeliveryStep.id)
            .withOrderNumber(1)
            .withTitle("How often will sessions take place?")
            .withHint("For example, every week, every 2 weeks, every month.")
            .withAnswerType(ActionPlanQuestionAnswerType.TEXTAREA)
            .withMaxNumberResponses(1)
            .create(),
        )

        val question2 = actionPlanStepQuestionRepository.save(
          ActionPlanStepQuestionFactory()
            .withActionPlanStepId(sessionDeliveryStep.id)
            .withOrderNumber(2)
            .withTitle("How will the sessions take place?")
            .withHint("Select one option.")
            .withAnswerType(ActionPlanQuestionAnswerType.RADIO)
            .withMaxNumberResponses(1)
            .create(),
        )

        actionPlanStepQuestionChoiceRepository.save(
          ActionPlanStepQuestionChoiceFactory()
            .withActionPlanStepQuestionId(question2.id)
            .withOrderNumber(1)
            .withLabel("In person")
            .withValue("IN_PERSON")
            .withHasFreeText(true)
            .withFreeTextLabel("Reason for not being in person")
            .withFreeTextHint("Why are the sessions not in person?")
            .create(),
        )
        actionPlanStepQuestionChoiceRepository.save(
          ActionPlanStepQuestionChoiceFactory()
            .withActionPlanStepQuestionId(question2.id)
            .withOrderNumber(2)
            .withLabel("Video call")
            .withValue("VIDEO_CALL")
            .withHasFreeText(true)
            .withFreeTextLabel("Reason for not being in person")
            .withFreeTextHint("Why are the sessions not in person?")
            .create(),
        )
        actionPlanStepQuestionChoiceRepository.save(
          ActionPlanStepQuestionChoiceFactory()
            .withActionPlanStepQuestionId(question2.id)
            .withOrderNumber(3)
            .withLabel("Phone call")
            .withValue("PHONE_CALL")
            .withHasFreeText(true)
            .withFreeTextLabel("Reason for not being in person")
            .withFreeTextHint("Why are the sessions not in person?")
            .create(),
        )

        val question3 = actionPlanStepQuestionRepository.save(
          ActionPlanStepQuestionFactory()
            .withActionPlanStepId(sessionDeliveryStep.id)
            .withOrderNumber(3)
            .withTitle("What format will you use for the sessions?")
            .withHint("Select all that apply.")
            .withAnswerType(ActionPlanQuestionAnswerType.CHECKBOX)
            .withMaxNumberResponses(2)
            .create(),
        )

        actionPlanStepQuestionChoiceRepository.save(
          ActionPlanStepQuestionChoiceFactory()
            .withActionPlanStepQuestionId(question3.id)
            .withOrderNumber(1)
            .withLabel("One-to-one session")
            .withValue("ONE_TO_ONE_SESSION")
            .create(),
        )
        actionPlanStepQuestionChoiceRepository.save(
          ActionPlanStepQuestionChoiceFactory()
            .withActionPlanStepQuestionId(question3.id)
            .withOrderNumber(2)
            .withLabel("Group session")
            .withValue("GROUP_SESSION")
            .create(),
        )

        webTestClient.get()
          .uri("/bff/referral/${referral.referenceNumber}/action-plan/session-delivery-details")
          .headers(setAuthorisation())
          .exchange()
          .expectStatus().isOk
          .expectBody<ActionPlanSessionDeliveryDetailsResponse>()
          .consumeWith { response ->
            val body = response.responseBody!!

            body.questions.size shouldBe 3
            body.questions[0].id shouldBe question1.id
            body.questions[0].label shouldBe "How often will sessions take place?"
            body.questions[0].hint shouldBe "For example, every week, every 2 weeks, every month."
            body.questions[0].answerType shouldBe ActionPlanQuestionAnswerType.TEXTAREA
            body.questions[0].maximumNumberOfResponses shouldBe 1
            body.questions[0].displayOrder shouldBe 1
            body.questions[0].savedResponses shouldBe emptyList()
            body.questions[0].choices shouldBe null

            body.questions[1].id shouldBe question2.id
            body.questions[1].label shouldBe "How will the sessions take place?"
            body.questions[1].hint shouldBe "Select one option."
            body.questions[1].answerType shouldBe ActionPlanQuestionAnswerType.RADIO
            body.questions[1].maximumNumberOfResponses shouldBe 1
            body.questions[1].displayOrder shouldBe 2
            body.questions[1].choices?.map { it.label } shouldBe listOf("In person", "Video call", "Phone call")
            body.questions[1].choices?.map { it.additionalDetailsHint } shouldBe listOf(
              "Why are the sessions not in person?",
              "Why are the sessions not in person?",
              "Why are the sessions not in person?",
            )

            body.questions[2].id shouldBe question3.id
            body.questions[2].label shouldBe "What format will you use for the sessions?"
            body.questions[2].hint shouldBe "Select all that apply."
            body.questions[2].answerType shouldBe ActionPlanQuestionAnswerType.CHECKBOX
            body.questions[2].maximumNumberOfResponses shouldBe 2
            body.questions[2].displayOrder shouldBe 3
            body.questions[2].choices?.map { it.label } shouldBe listOf("One-to-one session", "Group session")
          }
      }

      @Test
      fun `should return 404 when no session delivery step exists`() {
        val referral = createReferral("John", "Smith")
        val actionPlanTemplate = actionPlanHelper.createActionPlanTemplate()
        actionPlanHelper.createActionPlan(referralId = referral.id, templateId = actionPlanTemplate.id)

        webTestClient.get()
          .uri("/bff/referral/${referral.referenceNumber}/action-plan/session-delivery-details")
          .headers(setAuthorisation())
          .exchange()
          .expectStatus().isNotFound
      }

      @Test
      fun `should include saved responses from persisted question answers (radio)`() {
        val user = referralHelper.ensureReferralUser()
        val referral = createReferral("Peter", "Jones")
        val actionPlanTemplate = actionPlanHelper.createActionPlanTemplate()
        val actionPlan = actionPlanHelper.createActionPlan(referralId = referral.id, templateId = actionPlanTemplate.id)

        val sessionDeliveryStep = actionPlanStepRepository.save(
          ActionPlanStepFactory()
            .withActionPlanTemplateId(actionPlanTemplate.id)
            .withOrderNumber(2)
            .withName("Service Delivery Details")
            .withStepType(ActionPlanStepType.SESSION_DELIVERY)
            .create(),
        )

        val question = actionPlanStepQuestionRepository.save(
          ActionPlanStepQuestionFactory()
            .withActionPlanStepId(sessionDeliveryStep.id)
            .withOrderNumber(1)
            .withTitle("How many people will be in the session?")
            .withAnswerType(ActionPlanQuestionAnswerType.RADIO)
            .withMaxNumberResponses(1)
            .create(),
        )

        actionPlanStepQuestionChoiceRepository.save(
          ActionPlanStepQuestionChoiceFactory()
            .withActionPlanStepQuestionId(question.id)
            .withValue("ONE_TO_ONE")
            .withLabel("One-to-one")
            .withOrderNumber(1)
            .create(),
        )
        actionPlanStepQuestionChoiceRepository.save(
          ActionPlanStepQuestionChoiceFactory()
            .withActionPlanStepQuestionId(question.id)
            .withValue("IN_A_GROUP")
            .withLabel("In a group")
            .withHasFreeText(true)
            .withFreeTextLabel("How many people will be in the group?")
            .withOrderNumber(2)
            .create(),
        )

        val answerId = UUID.randomUUID()
        actionPlanStepQuestionAnswerHeaderRepository.save(
          ActionPlanStepQuestionAnswerHeader(
            id = answerId,
            actionPlanId = actionPlan.id,
            actionPlanStepQuestionId = question.id,
            orderNumber = 1,
            createdAt = OffsetDateTime.now(),
            createdBy = user.id.toString(),
          ),
        )

        actionPlanStepQuestionAnswerDetailsRepository.save(
          ActionPlanStepQuestionAnswerDetails(
            id = UUID.randomUUID(),
            actionPlanStepQuestionAnswerHeaderId = answerId,
            revisionNumber = 1,
            content = "IN_A_GROUP",
            freeTextValue = "3 people",
            createdAt = OffsetDateTime.now(),
            createdBy = user.id.toString(),
          ),
        )

        webTestClient.get()
          .uri("/bff/referral/${referral.referenceNumber}/action-plan/session-delivery-details")
          .headers(setAuthorisation())
          .exchange()
          .expectStatus().isOk
          .expectBody<ActionPlanSessionDeliveryDetailsResponse>()
          .consumeWith { response ->
            val body = response.responseBody!!

            body.questions.size shouldBe 1
            body.questions[0].maximumNumberOfResponses shouldBe 1
            body.questions[0].savedResponses.map { it.value } shouldBe listOf("IN_A_GROUP")
            body.questions[0].savedResponses.map { it.additionalDetails } shouldBe listOf("3 people")
          }
      }

      @Test
      fun `should include saved responses from persisted question answers (checkboxes)`() {
        val user = referralHelper.ensureReferralUser()
        val referral = createReferral("John", "Smith")
        val actionPlanTemplate = actionPlanHelper.createActionPlanTemplate()
        val actionPlan = actionPlanHelper.createActionPlan(referralId = referral.id, templateId = actionPlanTemplate.id)

        val sessionDeliveryStep = actionPlanStepRepository.save(
          ActionPlanStepFactory()
            .withActionPlanTemplateId(actionPlanTemplate.id)
            .withOrderNumber(2)
            .withName("Service Delivery Details")
            .withStepType(ActionPlanStepType.SESSION_DELIVERY)
            .create(),
        )

        val question = actionPlanStepQuestionRepository.save(
          ActionPlanStepQuestionFactory()
            .withActionPlanStepId(sessionDeliveryStep.id)
            .withOrderNumber(1)
            .withTitle("What communication methods will be used?")
            .withAnswerType(ActionPlanQuestionAnswerType.CHECKBOX)
            .withMaxNumberResponses(3)
            .create(),
        )

        actionPlanStepQuestionChoiceRepository.save(
          ActionPlanStepQuestionChoiceFactory()
            .withActionPlanStepQuestionId(question.id)
            .withValue("BY_PHONE")
            .withLabel("By phone")
            .withOrderNumber(1)
            .create(),
        )
        actionPlanStepQuestionChoiceRepository.save(
          ActionPlanStepQuestionChoiceFactory()
            .withActionPlanStepQuestionId(question.id)
            .withValue("BY_MESSAGE")
            .withLabel("By message")
            .withOrderNumber(2)
            .create(),
        )
        actionPlanStepQuestionChoiceRepository.save(
          ActionPlanStepQuestionChoiceFactory()
            .withActionPlanStepQuestionId(question.id)
            .withValue("BY_EMAIL")
            .withLabel("By email")
            .withOrderNumber(3)
            .create(),
        )

        val firstAnswerId = UUID.randomUUID()
        val secondAnswerId = UUID.randomUUID()
        val thirdAnswerId = UUID.randomUUID()
        actionPlanStepQuestionAnswerHeaderRepository.save(
          ActionPlanStepQuestionAnswerHeader(
            id = firstAnswerId,
            actionPlanId = actionPlan.id,
            actionPlanStepQuestionId = question.id,
            orderNumber = 1,
            createdAt = OffsetDateTime.now(),
            createdBy = user.id.toString(),
          ),
        )
        actionPlanStepQuestionAnswerHeaderRepository.save(
          ActionPlanStepQuestionAnswerHeader(
            id = secondAnswerId,
            actionPlanId = actionPlan.id,
            actionPlanStepQuestionId = question.id,
            orderNumber = 2,
            createdAt = OffsetDateTime.now(),
            createdBy = user.id.toString(),
          ),
        )
        actionPlanStepQuestionAnswerHeaderRepository.save(
          ActionPlanStepQuestionAnswerHeader(
            id = thirdAnswerId,
            actionPlanId = actionPlan.id,
            actionPlanStepQuestionId = question.id,
            orderNumber = 3,
            createdAt = OffsetDateTime.now(),
            createdBy = user.id.toString(),
          ),
        )

        actionPlanStepQuestionAnswerDetailsRepository.save(
          ActionPlanStepQuestionAnswerDetails(
            id = UUID.randomUUID(),
            actionPlanStepQuestionAnswerHeaderId = firstAnswerId,
            revisionNumber = 1,
            content = "BY_PHONE",
            createdAt = OffsetDateTime.now(),
            createdBy = user.id.toString(),
          ),
        )
        actionPlanStepQuestionAnswerDetailsRepository.save(
          ActionPlanStepQuestionAnswerDetails(
            id = UUID.randomUUID(),
            actionPlanStepQuestionAnswerHeaderId = secondAnswerId,
            revisionNumber = 1,
            content = "BY_MESSAGE",
            createdAt = OffsetDateTime.now(),
            createdBy = user.id.toString(),
          ),
        )
        actionPlanStepQuestionAnswerDetailsRepository.save(
          ActionPlanStepQuestionAnswerDetails(
            id = UUID.randomUUID(),
            actionPlanStepQuestionAnswerHeaderId = thirdAnswerId,
            revisionNumber = 1,
            content = "BY_EMAIL",
            createdAt = OffsetDateTime.now(),
            createdBy = user.id.toString(),
          ),
        )

        webTestClient.get()
          .uri("/bff/referral/${referral.referenceNumber}/action-plan/session-delivery-details")
          .headers(setAuthorisation())
          .exchange()
          .expectStatus().isOk
          .expectBody<ActionPlanSessionDeliveryDetailsResponse>()
          .consumeWith { response ->
            val body = response.responseBody!!

            body.questions.size shouldBe 1
            body.questions[0].maximumNumberOfResponses shouldBe 3
            body.questions[0].savedResponses.map { it.value } shouldBe listOf("BY_PHONE", "BY_MESSAGE", "BY_EMAIL")
          }
      }

      @Test
      fun `should return not found for unknown referral reference`() {
        assertNotFound(GET, "/bff/referral/ZZ9999ZZ/action-plan/session-delivery-details")
      }
    }

    @Nested
    @DisplayName("PATCH /referral/{referralReference}/action-plan/session-delivery-details")
    inner class PatchSessionDeliveryDetailsEndpoint {
      @Test
      fun `should return unauthorized if no token`() {
        assertUnauthorized(PATCH, "/referral/AB1234CD/action-plan/session-delivery-details")
      }

      @Test
      fun `should return forbidden if no role`() {
        assertForbiddenNoRole(
          PATCH,
          "/referral/AB1234CD/action-plan/session-delivery-details",
          ActionPlanSessionDeliveryDetailsRequest(answers = emptyList()),
        )
      }

      @Test
      fun `should return forbidden if wrong role`() {
        assertForbiddenWrongRole(
          PATCH,
          "/referral/AB1234CD/action-plan/session-delivery-details",
          ActionPlanSessionDeliveryDetailsRequest(answers = emptyList()),
        )
      }

      @Test
      fun `should save and then update session delivery answers`() {
        whenever(userMapper.fromToken(any<HmppsAuthenticationHolder>())).thenReturn(testUser)

        val referral = createReferral("Lucy", "Miles")
        val actionPlanTemplate = actionPlanHelper.createActionPlanTemplate()
        val actionPlan = actionPlanHelper.createActionPlan(referralId = referral.id, templateId = actionPlanTemplate.id)

        val sessionDeliveryStep = actionPlanStepRepository.save(
          ActionPlanStepFactory()
            .withActionPlanTemplateId(actionPlanTemplate.id)
            .withOrderNumber(2)
            .withName("Service Delivery Details")
            .withStepType(ActionPlanStepType.SESSION_DELIVERY)
            .create(),
        )

        val radioQuestion = actionPlanStepQuestionRepository.save(
          ActionPlanStepQuestionFactory()
            .withActionPlanStepId(sessionDeliveryStep.id)
            .withOrderNumber(1)
            .withTitle("How will the session be delivered?")
            .withAnswerType(ActionPlanQuestionAnswerType.RADIO)
            .withMaxNumberResponses(1)
            .create(),
        )

        actionPlanStepQuestionChoiceRepository.save(
          ActionPlanStepQuestionChoiceFactory()
            .withActionPlanStepQuestionId(radioQuestion.id)
            .withOrderNumber(1)
            .withLabel("Face-to-face")
            .withValue("FACE_TO_FACE")
            .create(),
        )
        actionPlanStepQuestionChoiceRepository.save(
          ActionPlanStepQuestionChoiceFactory()
            .withActionPlanStepQuestionId(radioQuestion.id)
            .withOrderNumber(2)
            .withLabel("Other")
            .withValue("OTHER")
            .withHasFreeText(true)
            .withFreeTextLabel("Reason")
            .create(),
        )

        val secondQuestion = actionPlanStepQuestionRepository.save(
          ActionPlanStepQuestionFactory()
            .withActionPlanStepId(sessionDeliveryStep.id)
            .withOrderNumber(2)
            .withTitle("What is the session length?")
            .withAnswerType(ActionPlanQuestionAnswerType.RADIO)
            .withMaxNumberResponses(1)
            .create(),
        )

        actionPlanStepQuestionChoiceRepository.save(
          ActionPlanStepQuestionChoiceFactory()
            .withActionPlanStepQuestionId(secondQuestion.id)
            .withOrderNumber(1)
            .withLabel("Short session")
            .withValue("SHORT_SESSION")
            .create(),
        )

        val saveRequest = ActionPlanSessionDeliveryDetailsRequest(
          answers = listOf(
            SessionDeliveryDetailsQuestionAnswers(
              questionId = radioQuestion.id,
              incomingAnswerDetails = listOf(
                SessionDeliveryDetailsQuestionAnswer(value = "OTHER", additionalDetails = "Poor weather"),
              ),
            ),
            SessionDeliveryDetailsQuestionAnswers(
              questionId = secondQuestion.id,
              incomingAnswerDetails = listOf(
                SessionDeliveryDetailsQuestionAnswer(value = "SHORT_SESSION"),
              ),
            ),
          ),
        )

        webTestClient.patch()
          .uri("/referral/${referral.referenceNumber}/action-plan/session-delivery-details")
          .headers(setAuthorisation())
          .bodyValue(saveRequest)
          .exchange()
          .expectStatus().isOk
          .expectBody<ActionPlanSessionDeliveryDetailsResponse>()
          .consumeWith { response ->
            val body = response.responseBody!!
            body.questions.first { it.id == radioQuestion.id }.savedResponses.map { it.value } shouldBe listOf("OTHER")
            body.questions.first { it.id == radioQuestion.id }.savedResponses.map { it.additionalDetails } shouldBe listOf("Poor weather")
            body.questions.first { it.id == secondQuestion.id }.savedResponses.map { it.value } shouldBe listOf("SHORT_SESSION")
          }

        val updateRequest = ActionPlanSessionDeliveryDetailsRequest(
          answers = listOf(
            SessionDeliveryDetailsQuestionAnswers(
              questionId = radioQuestion.id,
              incomingAnswerDetails = listOf(
                SessionDeliveryDetailsQuestionAnswer(value = "FACE_TO_FACE"),
              ),
            ),
            SessionDeliveryDetailsQuestionAnswers(
              questionId = secondQuestion.id,
              incomingAnswerDetails = emptyList(),
            ),
          ),
        )

        webTestClient.patch()
          .uri("/referral/${referral.referenceNumber}/action-plan/session-delivery-details")
          .headers(setAuthorisation())
          .bodyValue(updateRequest)
          .exchange()
          .expectStatus().isOk
          .expectBody<ActionPlanSessionDeliveryDetailsResponse>()
          .consumeWith { response ->
            val body = response.responseBody!!
            body.questions.first { it.id == radioQuestion.id }.savedResponses.map { it.value } shouldBe listOf("FACE_TO_FACE")
            body.questions.first { it.id == radioQuestion.id }.savedResponses.map { it.additionalDetails } shouldBe listOf(null)
            body.questions.first { it.id == secondQuestion.id }.savedResponses shouldBe emptyList()
          }

        val activeAnswers = actionPlanStepQuestionAnswerHeaderRepository.findAllByActionPlanIdAndDeletedAtIsNull(actionPlan.id)
        activeAnswers.filter { it.actionPlanStepQuestionId == radioQuestion.id }.size shouldBe 1
        activeAnswers.filter { it.actionPlanStepQuestionId == secondQuestion.id }.size shouldBe 0

        val allAnswers = actionPlanStepQuestionAnswerHeaderRepository.findAll()
        allAnswers
          .filter { it.actionPlanId == actionPlan.id && it.actionPlanStepQuestionId == secondQuestion.id }
          .count { it.deletedAt != null } shouldBe 1

        val updatedRadioAnswer = activeAnswers.first { it.actionPlanStepQuestionId == radioQuestion.id }
        val radioRevisions = actionPlanStepQuestionAnswerDetailsRepository
          .findAllByActionPlanStepQuestionAnswerHeaderIdIn(listOf(updatedRadioAnswer.id))
          .sortedBy { it.revisionNumber }
        radioRevisions.map { it.content } shouldBe listOf("OTHER", "FACE_TO_FACE")
        radioRevisions.map { it.freeTextValue } shouldBe listOf("Poor weather", null)
      }

      @Test
      fun `should return not found for unknown referral reference`() {
        whenever(userMapper.fromToken(any<HmppsAuthenticationHolder>())).thenReturn(testUser)

        assertNotFound(
          PATCH,
          "/referral/ZZ9999ZZ/action-plan/session-delivery-details",
          ActionPlanSessionDeliveryDetailsRequest(answers = emptyList()),
        )
      }
    }

    @Nested
    @DisplayName("POST /referral/{referralReference}/action-plan/action")
    inner class PostActionEndpoint {
      @Test
      fun `should return unauthorized if no token`() {
        assertUnauthorized(
          org.springframework.http.HttpMethod.POST,
          "/referral/AB1234CD/action-plan/action",
        )
      }

      @Test
      fun `should return forbidden if no role`() {
        assertForbiddenNoRole(
          org.springframework.http.HttpMethod.POST,
          "/referral/AB1234CD/action-plan/action",
          ActionPlanActionRequest(
            needId = UUID.randomUUID(),
            outcomeId = UUID.randomUUID(),
            activities = listOf(
              ActionPlanActivityRequest(
                who = "test",
                activityDetails = "test",
                status = "test",
              ),
            ),
          ),
        )
      }

      @Test
      fun `should return forbidden if wrong role`() {
        assertForbiddenWrongRole(
          org.springframework.http.HttpMethod.POST,
          "/referral/AB1234CD/action-plan/action",
          ActionPlanActionRequest(
            needId = UUID.randomUUID(),
            outcomeId = UUID.randomUUID(),
            activities = listOf(
              ActionPlanActivityRequest(
                who = "test",
                activityDetails = "test",
                status = "test",
              ),
            ),
          ),
        )
      }

      @Test
      fun `should return 404 when referral not found`() {
        whenever(userMapper.fromToken(any<HmppsAuthenticationHolder>())).thenReturn(testUser)
        val needId = needRepository.findAllByOrderByOrderNumberAsc().first().id
        val outcomeId = needRepository.findAllByOrderByOrderNumberAsc().first().outcomes.first().id

        webTestClient.post()
          .uri("/referral/INVALID-REF/action-plan/action")
          .headers(setAuthorisation())
          .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
          .bodyValue(
            ActionPlanActionRequest(
              needId = needId,
              outcomeId = outcomeId,
              activities = listOf(
                ActionPlanActivityRequest(
                  who = "Service provider",
                  activityDetails = "Weekly session",
                  status = "Active",
                ),
              ),
            ),
          )
          .exchange()
          .expectStatus().isNotFound
      }

      @Test
      fun `should submit action with need, outcome and activities successfully`() {
        whenever(userMapper.fromToken(any<HmppsAuthenticationHolder>())).thenReturn(testUser)

        val referral = createReferral("Sarah", "Williams")
        val actionPlanTemplate = actionPlanHelper.createActionPlanTemplate()
        val actionPlan = actionPlanHelper.createActionPlan(referralId = referral.id, templateId = actionPlanTemplate.id)

        val needStep = actionPlanStepRepository.save(
          ActionPlanStepFactory()
            .withActionPlanTemplateId(actionPlanTemplate.id)
            .withOrderNumber(1)
            .withName("Select a Need")
            .withStepType(ActionPlanStepType.NEED)
            .create(),
        )

        val need = needRepository.findAllByOrderByOrderNumberAsc().first()
        val outcome = need.outcomes.first()

        val question = actionPlanStepQuestionRepository.save(
          ActionPlanStepQuestionFactory()
            .withActionPlanStepId(needStep.id)
            .withOrderNumber(1)
            .withTitle("Select an outcome for ${need.label}")
            .withAnswerType(ActionPlanQuestionAnswerType.RADIO)
            .withQuestionType(ActionPlanQuestionType.OUTCOME)
            .withMaxNumberResponses(1)
            .withNeedId(need.id)
            .create(),
        )

        val activities = listOf(
          ActionPlanActivityRequest(
            who = "Service provider",
            activityDetails = "Weekly one-to-one sessions",
            status = "Active",
          ),
          ActionPlanActivityRequest(
            who = "Service user",
            activityDetails = "Attend all sessions",
            status = "Active",
          ),
        )

        webTestClient.post()
          .uri("/referral/${referral.referenceNumber}/action-plan/action")
          .headers(setAuthorisation())
          .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
          .bodyValue(
            ActionPlanActionRequest(
              needId = need.id,
              outcomeId = outcome.id,
              activities = activities,
            ),
          )
          .exchange()
          .expectStatus().isOk
          .expectBody<ActionPlanActionResponse>()
          .consumeWith { response ->
            val body = response.responseBody!!
            body.success shouldBe true
            body.message shouldBe "Action submitted successfully"
          }

        val savedAnswerHeader = actionPlanStepQuestionAnswerHeaderRepository
          .findActiveByPlanAndQuestionIds(actionPlan.id, listOf(question.id))
          .first()

        val savedAnswerDetails = actionPlanStepQuestionAnswerDetailsRepository
          .findAllByActionPlanStepQuestionAnswerHeaderIdIn(listOf(savedAnswerHeader.id))
          .first()

        savedAnswerDetails.content shouldBe outcome.id.toString()

        val savedActivities = actionPlanActivityRepository
          .findByActionPlanStepQuestionAnswerHeaderId(savedAnswerHeader.id)

        savedActivities.size shouldBe 2
        savedActivities[0].who shouldBe "Service provider"
        savedActivities[0].activityDetails shouldBe "Weekly one-to-one sessions"
        savedActivities[0].status shouldBe "Active"
        savedActivities[1].who shouldBe "Service user"
        savedActivities[1].activityDetails shouldBe "Attend all sessions"
        savedActivities[1].status shouldBe "Active"
      }

      @Test
      fun `should replace existing action when submitting new action for same need`() {
        whenever(userMapper.fromToken(any<HmppsAuthenticationHolder>())).thenReturn(testUser)

        val referral = createReferral("Thomas", "Brown")
        val actionPlanTemplate = actionPlanHelper.createActionPlanTemplate()
        val actionPlan = actionPlanHelper.createActionPlan(referralId = referral.id, templateId = actionPlanTemplate.id)

        val needStep = actionPlanStepRepository.save(
          ActionPlanStepFactory()
            .withActionPlanTemplateId(actionPlanTemplate.id)
            .withOrderNumber(1)
            .withName("Select a Need")
            .withStepType(ActionPlanStepType.NEED)
            .create(),
        )

        val need = needRepository.findAllByOrderByOrderNumberAsc().first()
        val outcomes = need.outcomes
        val firstOutcome = outcomes[0]
        val secondOutcome = outcomes[1]

        val question = actionPlanStepQuestionRepository.save(
          ActionPlanStepQuestionFactory()
            .withActionPlanStepId(needStep.id)
            .withOrderNumber(1)
            .withTitle("Select an outcome for ${need.label}")
            .withAnswerType(ActionPlanQuestionAnswerType.RADIO)
            .withQuestionType(ActionPlanQuestionType.OUTCOME)
            .withMaxNumberResponses(1)
            .withNeedId(need.id)
            .create(),
        )

        webTestClient.post()
          .uri("/referral/${referral.referenceNumber}/action-plan/action")
          .headers(setAuthorisation())
          .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
          .bodyValue(
            ActionPlanActionRequest(
              needId = need.id,
              outcomeId = firstOutcome.id,
              activities = listOf(
                ActionPlanActivityRequest(
                  who = "Provider",
                  activityDetails = "First set of activities",
                  status = "Active",
                ),
              ),
            ),
          )
          .exchange()
          .expectStatus().isOk

        val firstAnswerHeaders = actionPlanStepQuestionAnswerHeaderRepository
          .findActiveByPlanAndQuestionIds(actionPlan.id, listOf(question.id))

        firstAnswerHeaders.size shouldBe 1

        webTestClient.post()
          .uri("/referral/${referral.referenceNumber}/action-plan/action")
          .headers(setAuthorisation())
          .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
          .bodyValue(
            ActionPlanActionRequest(
              needId = need.id,
              outcomeId = secondOutcome.id,
              activities = listOf(
                ActionPlanActivityRequest(
                  who = "Provider",
                  activityDetails = "Second set of activities",
                  status = "Active",
                ),
              ),
            ),
          )
          .exchange()
          .expectStatus().isOk

        val secondAnswerHeaders = actionPlanStepQuestionAnswerHeaderRepository
          .findActiveByPlanAndQuestionIds(actionPlan.id, listOf(question.id))

        secondAnswerHeaders.size shouldBe 1
        secondAnswerHeaders.first().id shouldBe firstAnswerHeaders.first().id

        val secondAnswerDetails = actionPlanStepQuestionAnswerDetailsRepository
          .findAllByActionPlanStepQuestionAnswerHeaderIdIn(listOf(secondAnswerHeaders.first().id))
          .maxByOrNull { it.revisionNumber }
          ?: error("Expected answer details for updated action")

        secondAnswerDetails.content shouldBe secondOutcome.id.toString()

        val secondActivities = actionPlanActivityRepository
          .findByActionPlanStepQuestionAnswerHeaderId(secondAnswerHeaders.first().id)

        secondActivities.size shouldBe 1
        secondActivities.first().activityDetails shouldBe "Second set of activities"
      }

      @Test
      fun `should return 400 when outcome does not belong to need`() {
        whenever(userMapper.fromToken(any<HmppsAuthenticationHolder>())).thenReturn(testUser)

        val referral = createReferral("Michael", "Davis")
        val actionPlanTemplate = actionPlanHelper.createActionPlanTemplate()
        actionPlanHelper.createActionPlan(referralId = referral.id, templateId = actionPlanTemplate.id)

        val needs = needRepository.findAllByOrderByOrderNumberAsc()
        val firstNeed = needs[0]
        val secondNeed = needs[1]
        val wrongOutcome = secondNeed.outcomes.first()

        webTestClient.post()
          .uri("/referral/${referral.referenceNumber}/action-plan/action")
          .headers(setAuthorisation())
          .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
          .bodyValue(
            ActionPlanActionRequest(
              needId = firstNeed.id,
              outcomeId = wrongOutcome.id,
              activities = listOf(
                ActionPlanActivityRequest(
                  who = "Provider",
                  activityDetails = "Activities",
                  status = "Active",
                ),
              ),
            ),
          )
          .exchange()
          .expectStatus().isBadRequest
      }

      @Test
      fun `should return 400 when activities list is empty`() {
        whenever(userMapper.fromToken(any<HmppsAuthenticationHolder>())).thenReturn(testUser)

        val referral = createReferral("Jennifer", "Miller")
        val actionPlanTemplate = actionPlanHelper.createActionPlanTemplate()
        actionPlanHelper.createActionPlan(referralId = referral.id, templateId = actionPlanTemplate.id)

        val need = needRepository.findAllByOrderByOrderNumberAsc().first()
        val outcome = need.outcomes.first()

        webTestClient.post()
          .uri("/referral/${referral.referenceNumber}/action-plan/action")
          .headers(setAuthorisation())
          .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
          .bodyValue(
            ActionPlanActionRequest(
              needId = need.id,
              outcomeId = outcome.id,
              activities = emptyList(),
            ),
          )
          .exchange()
          .expectStatus().isBadRequest
      }

      @Test
      fun `should return 400 when activity field is blank`() {
        whenever(userMapper.fromToken(any<HmppsAuthenticationHolder>())).thenReturn(testUser)

        val referral = createReferral("Robert", "Taylor")
        val actionPlanTemplate = actionPlanHelper.createActionPlanTemplate()
        actionPlanHelper.createActionPlan(referralId = referral.id, templateId = actionPlanTemplate.id)

        val need = needRepository.findAllByOrderByOrderNumberAsc().first()
        val outcome = need.outcomes.first()

        webTestClient.post()
          .uri("/referral/${referral.referenceNumber}/action-plan/action")
          .headers(setAuthorisation())
          .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
          .bodyValue(
            ActionPlanActionRequest(
              needId = need.id,
              outcomeId = outcome.id,
              activities = listOf(
                ActionPlanActivityRequest(
                  who = "",
                  activityDetails = "Activities",
                  status = "Active",
                ),
              ),
            ),
          )
          .exchange()
          .expectStatus().isBadRequest
      }

      @Test
      fun `should create action plan if it does not exist`() {
        whenever(userMapper.fromToken(any<HmppsAuthenticationHolder>())).thenReturn(testUser)

        val referral = createReferral("Alice", "Johnson")
        // Find or create the activeGlobal template - there's a unique constraint so only one can exist
        val actionPlanTemplate = actionPlanTemplateRepository.findFirstByActiveGlobalTrueOrderByIdAsc()
          ?: actionPlanHelper.createActionPlanTemplate(activeGlobal = true)
        // Note: NOT creating an action plan - it should be auto-created

        val need = needRepository.findAllByOrderByOrderNumberAsc().first()
        val outcome = need.outcomes.first()

        // Find the NEED step in the template, or create it if it doesn't exist
        val needSteps = actionPlanStepRepository
          .findAllByActionPlanTemplateIdOrderByOrderNumberAsc(actionPlanTemplate.id)
          .filter { it.stepType == ActionPlanStepType.NEED }

        val needStep = if (needSteps.isNotEmpty()) {
          needSteps.first()
        } else {
          actionPlanStepRepository.save(
            ActionPlanStepFactory()
              .withActionPlanTemplateId(actionPlanTemplate.id)
              .withOrderNumber(1)
              .withName("Needs")
              .withStepType(ActionPlanStepType.NEED)
              .create(),
          )
        }

        // Find or create the outcome question for this need
        val questions = actionPlanStepQuestionRepository
          .findAllByActionPlanStepIdInOrderByOrderNumberAsc(listOf(needStep.id))
          .filter { it.questionType == ActionPlanQuestionType.OUTCOME && it.needId == need.id }

        val question = if (questions.isNotEmpty()) {
          questions.first()
        } else {
          actionPlanStepQuestionRepository.save(
            ActionPlanStepQuestionFactory()
              .withActionPlanStepId(needStep.id)
              .withOrderNumber(1)
              .withTitle("What is the desired outcome?")
              .withAnswerType(ActionPlanQuestionAnswerType.TEXTAREA)
              .withQuestionType(ActionPlanQuestionType.OUTCOME)
              .withMaxNumberResponses(10)
              .withNeedId(need.id)
              .create(),
          )
        }

        webTestClient.post()
          .uri("/referral/${referral.referenceNumber}/action-plan/action")
          .headers(setAuthorisation())
          .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
          .bodyValue(
            ActionPlanActionRequest(
              needId = need.id,
              outcomeId = outcome.id,
              activities = listOf(
                ActionPlanActivityRequest(
                  who = "Service provider",
                  activityDetails = "Support activities",
                  status = "Active",
                ),
              ),
            ),
          )
          .exchange()
          .expectStatus().isOk
          .expectBody<ActionPlanActionResponse>()
          .consumeWith { response ->
            response.responseBody shouldNotBe null
          }

        val savedActionPlan = actionPlanRepository.findByReferralId(referral.id)
        savedActionPlan shouldNotBe null

        val savedAnswerHeader = actionPlanStepQuestionAnswerHeaderRepository
          .findActiveByPlanAndQuestionIds(savedActionPlan!!.id, listOf(question.id))
          .first()

        val savedActivities = actionPlanActivityRepository
          .findByActionPlanStepQuestionAnswerHeaderId(savedAnswerHeader.id)

        savedActivities.size shouldBe 1
        savedActivities.first().activityDetails shouldBe "Support activities"
      }
    }

    private fun createReferral(firstName: String, lastName: String): Referral {
      val user = referralHelper.ensureReferralUser()
      val person = referralHelper.createPerson(firstName = firstName, lastName = lastName)
      return referralHelper.createReferral(person = person, referenceNumber = randomReferralReference(), submittedBy = user)
    }
  }
}
