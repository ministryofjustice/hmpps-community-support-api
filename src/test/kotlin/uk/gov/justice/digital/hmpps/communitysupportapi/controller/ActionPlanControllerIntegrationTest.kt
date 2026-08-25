package uk.gov.justice.digital.hmpps.communitysupportapi.controller

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpMethod.PATCH
import org.springframework.test.web.reactive.server.expectBody
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.ActionPlanNeedsResponse
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.ActionPlanSessionDeliveryDetailsRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.ActionPlanSessionDeliveryDetailsResponse
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.ActionPlanSummaryDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.SavedResponse
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.SessionDeliveryQuestionRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanQuestionAnswerType
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanStepQuestionAnswer
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanStepQuestionAnswerRevision
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanStepType
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Referral
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.ActionPlanTestSupport
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.ReferralTestSupport
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ActionPlanStepQuestionAnswerRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ActionPlanStepQuestionAnswerRevisionRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ActionPlanStepQuestionChoiceRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ActionPlanStepQuestionRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ActionPlanStepRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.NeedRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.ActionPlanStepFactory
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.ActionPlanStepQuestionChoiceFactory
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.ActionPlanStepQuestionFactory
import uk.gov.justice.digital.hmpps.communitysupportapi.util.ReferralReferenceTestUtil.randomReferralReference
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
  private lateinit var actionPlanStepQuestionAnswerRepository: ActionPlanStepQuestionAnswerRepository

  @Autowired
  private lateinit var actionPlanStepQuestionAnswerRevisionRepository: ActionPlanStepQuestionAnswerRevisionRepository

  @Nested
  @DisplayName("GET /bff/referral/{referralReference}/action-plan")
  inner class GetActionPlanSummaryEndpoint {
    @Test
    fun `should return OK with action plan summary for a valid referral reference`() {
      val user = referralHelper.ensureReferralUser()
      val person = referralHelper.createPerson(firstName = "Adam", lastName = "Smith")
      val referral =
        referralHelper.createReferral(person = person, referenceNumber = randomReferralReference(), submittedBy = user)
      val expectedNeeds = needRepository.findAllByOrderByOrderNumberAsc()

      webTestClient.get()
        .uri("/bff/referral/${referral.referenceNumber}/action-plan")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus().isOk
        .expectBody<ActionPlanSummaryDto>()
        .consumeWith { response ->
          val body = response.responseBody!!
          body.personDetails.fullName shouldBe "Adam Smith"
          body.needs.size shouldBe expectedNeeds.size
          body.needs.map { it.label } shouldBe expectedNeeds.map { it.label }
          body.needs.map { it.id } shouldBe expectedNeeds.map { it.id }
        }
    }

    @Nested
    @DisplayName("GET /bff/referral/{referralReference}/action-plan/needs")
    inner class GetActionPlanNeedsEndpoint {
      @Test
      fun `should return unauthorized if no token`() {
        assertUnauthorized(GET, "/bff/referral/AB1234CD/action-plan/needs")
      }

      @Test
      fun `should return forbidden if no role`() {
        assertForbiddenNoRole(GET, "/bff/referral/AB1234CD/action-plan/needs")
      }

      @Test
      fun `should return forbidden if wrong role`() {
        assertForbiddenWrongRole(GET, "/bff/referral/AB1234CD/action-plan/needs")
      }

      @Test
      fun `should return action plan needs grouped by need and ordered by question order`() {
        val referral = createReferral("Jo", "Bloggs")
        val actionPlanTemplate = actionPlanHelper.createActionPlanTemplate()
        actionPlanHelper.createActionPlan(referralId = referral.id, templateId = actionPlanTemplate.id)

        val orderedNeeds = needRepository.findAllByOrderByOrderNumberAsc().take(2)
        val firstNeed = orderedNeeds[0]
        val secondNeed = orderedNeeds[1]

        val needStep = actionPlanStepRepository.save(
          ActionPlanStepFactory()
            .withActionPlanTemplateId(actionPlanTemplate.id)
            .withOrderNumber(1)
            .withName("Needs")
            .withStepType(ActionPlanStepType.NEED)
            .create(),
        )

        actionPlanStepQuestionRepository.save(
          ActionPlanStepQuestionFactory()
            .withActionPlanStepId(needStep.id)
            .withOrderNumber(1)
            .withTitle("Question for second need")
            .withAnswerType(ActionPlanQuestionAnswerType.TEXTAREA)
            .withNeedId(secondNeed.id)
            .create(),
        )
        actionPlanStepQuestionRepository.save(
          ActionPlanStepQuestionFactory()
            .withActionPlanStepId(needStep.id)
            .withOrderNumber(2)
            .withTitle("First question for first need")
            .withAnswerType(ActionPlanQuestionAnswerType.TEXTAREA)
            .withNeedId(firstNeed.id)
            .create(),
        )
        actionPlanStepQuestionRepository.save(
          ActionPlanStepQuestionFactory()
            .withActionPlanStepId(needStep.id)
            .withOrderNumber(3)
            .withTitle("Second question for first need")
            .withAnswerType(ActionPlanQuestionAnswerType.TEXTAREA)
            .withNeedId(firstNeed.id)
            .create(),
        )
        actionPlanStepQuestionRepository.save(
          ActionPlanStepQuestionFactory()
            .withActionPlanStepId(needStep.id)
            .withOrderNumber(4)
            .withTitle("Question without need")
            .withNeedId(null)
            .create(),
        )

        webTestClient.get()
          .uri("/bff/referral/${referral.referenceNumber}/action-plan/needs")
          .headers(setAuthorisation())
          .exchange()
          .expectStatus().isOk
          .expectBody<ActionPlanNeedsResponse>()
          .consumeWith { response ->
            val body = response.responseBody!!

            body.needs.map { it.id } shouldBe listOf(firstNeed.id, secondNeed.id)
            body.needs[0].label shouldBe "Accommodation"
            body.needs[0].questions.map { it.label } shouldBe listOf(
              "First question for first need",
              "Second question for first need",
            )
            body.needs[0].questions.map { it.answerType } shouldBe listOf(
              ActionPlanQuestionAnswerType.TEXTAREA,
              ActionPlanQuestionAnswerType.TEXTAREA,
            )

            body.needs[1].label shouldBe secondNeed.label
            body.needs[1].questions.map { it.label } shouldBe listOf("Question for second need")
            body.needs[1].questions.map { it.answerType } shouldBe listOf(ActionPlanQuestionAnswerType.TEXTAREA)
          }
      }

      @Test
      fun `should return not found for unknown referral reference`() {
        assertNotFound(GET, "/bff/referral/ZZ9999ZZ/action-plan/needs")
      }

      private fun createReferral(firstName: String, lastName: String): Referral {
        val user = referralHelper.ensureReferralUser()
        val person = referralHelper.createPerson(firstName = firstName, lastName = lastName)
        return referralHelper.createReferral(
          person = person,
          referenceNumber = randomReferralReference(),
          submittedBy = user,
        )
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
            .withTitle("How will the session be delivered?")
            .withAnswerType(ActionPlanQuestionAnswerType.RADIO)
            .withMaxNumberResponses(1)
            .create(),
        )

        actionPlanStepQuestionChoiceRepository.save(
          ActionPlanStepQuestionChoiceFactory()
            .withActionPlanStepQuestionId(question1.id)
            .withOrderNumber(1)
            .withLabel("Face-to-face")
            .withValue("FACE_TO_FACE")
            .create(),
        )
        actionPlanStepQuestionChoiceRepository.save(
          ActionPlanStepQuestionChoiceFactory()
            .withActionPlanStepQuestionId(question1.id)
            .withOrderNumber(2)
            .withLabel("Other")
            .withValue("OTHER")
            .withHasFreeText(true)
            .withFreeTextLabel("Reason for not meeting face-to-face")
            .create(),
        )

        val question2 = actionPlanStepQuestionRepository.save(
          ActionPlanStepQuestionFactory()
            .withActionPlanStepId(sessionDeliveryStep.id)
            .withOrderNumber(2)
            .withTitle("How many sessions are required?")
            .withAnswerType(ActionPlanQuestionAnswerType.TEXTAREA)
            .withMaxNumberResponses(1)
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

            body.questions.size shouldBe 2
            body.questions[0].id shouldBe question1.id
            body.questions[0].label shouldBe "How will the session be delivered?"
            body.questions[0].answerType shouldBe ActionPlanQuestionAnswerType.RADIO
            body.questions[0].maximumNumberOfResponses shouldBe 1
            body.questions[0].displayOrder shouldBe 1
            body.questions[0].savedResponses shouldBe emptyList()
            body.questions[0].choices?.map { it.value } shouldBe listOf("FACE_TO_FACE", "OTHER")
            body.questions[0].choices?.map { it.label } shouldBe listOf("Face-to-face", "Other")
            body.questions[0].choices?.get(1)?.displayAdditionalDetailsOnSelect shouldBe true
            body.questions[0].choices?.get(1)?.additionalDetailsLabel shouldBe "Reason for not meeting face-to-face"

            body.questions[1].id shouldBe question2.id
            body.questions[1].label shouldBe "How many sessions are required?"
            body.questions[1].answerType shouldBe ActionPlanQuestionAnswerType.TEXTAREA
            body.questions[1].maximumNumberOfResponses shouldBe 1
            body.questions[1].displayOrder shouldBe 2
          }
      }

      @Test
      fun `should return empty questions when no session delivery step exists`() {
        val referral = createReferral("John", "Smith")
        val actionPlanTemplate = actionPlanHelper.createActionPlanTemplate()
        actionPlanHelper.createActionPlan(referralId = referral.id, templateId = actionPlanTemplate.id)

        webTestClient.get()
          .uri("/bff/referral/${referral.referenceNumber}/action-plan/session-delivery-details")
          .headers(setAuthorisation())
          .exchange()
          .expectStatus().isOk
          .expectBody<ActionPlanSessionDeliveryDetailsResponse>()
          .consumeWith { response ->
            val body = response.responseBody!!
            body.questions shouldBe emptyList()
          }
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
        actionPlanStepQuestionAnswerRepository.save(
          ActionPlanStepQuestionAnswer(
            id = answerId,
            actionPlanId = actionPlan.id,
            actionPlanStepQuestionId = question.id,
            orderNumber = 1,
            createdAt = OffsetDateTime.now(),
            createdBy = user.id.toString(),
          ),
        )

        actionPlanStepQuestionAnswerRevisionRepository.save(
          ActionPlanStepQuestionAnswerRevision(
            id = UUID.randomUUID(),
            actionPlanStepQuestionAnswerId = answerId,
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
        actionPlanStepQuestionAnswerRepository.save(
          ActionPlanStepQuestionAnswer(
            id = firstAnswerId,
            actionPlanId = actionPlan.id,
            actionPlanStepQuestionId = question.id,
            orderNumber = 1,
            createdAt = OffsetDateTime.now(),
            createdBy = user.id.toString(),
          ),
        )
        actionPlanStepQuestionAnswerRepository.save(
          ActionPlanStepQuestionAnswer(
            id = secondAnswerId,
            actionPlanId = actionPlan.id,
            actionPlanStepQuestionId = question.id,
            orderNumber = 2,
            createdAt = OffsetDateTime.now(),
            createdBy = user.id.toString(),
          ),
        )
        actionPlanStepQuestionAnswerRepository.save(
          ActionPlanStepQuestionAnswer(
            id = thirdAnswerId,
            actionPlanId = actionPlan.id,
            actionPlanStepQuestionId = question.id,
            orderNumber = 3,
            createdAt = OffsetDateTime.now(),
            createdBy = user.id.toString(),
          ),
        )

        actionPlanStepQuestionAnswerRevisionRepository.save(
          ActionPlanStepQuestionAnswerRevision(
            id = UUID.randomUUID(),
            actionPlanStepQuestionAnswerId = firstAnswerId,
            revisionNumber = 1,
            content = "BY_PHONE",
            createdAt = OffsetDateTime.now(),
            createdBy = user.id.toString(),
          ),
        )
        actionPlanStepQuestionAnswerRevisionRepository.save(
          ActionPlanStepQuestionAnswerRevision(
            id = UUID.randomUUID(),
            actionPlanStepQuestionAnswerId = secondAnswerId,
            revisionNumber = 1,
            content = "BY_MESSAGE",
            createdAt = OffsetDateTime.now(),
            createdBy = user.id.toString(),
          ),
        )
        actionPlanStepQuestionAnswerRevisionRepository.save(
          ActionPlanStepQuestionAnswerRevision(
            id = UUID.randomUUID(),
            actionPlanStepQuestionAnswerId = thirdAnswerId,
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
          ActionPlanSessionDeliveryDetailsRequest(questions = emptyList()),
        )
      }

      @Test
      fun `should return forbidden if wrong role`() {
        assertForbiddenWrongRole(
          PATCH,
          "/referral/AB1234CD/action-plan/session-delivery-details",
          ActionPlanSessionDeliveryDetailsRequest(questions = emptyList()),
        )
      }

      @Test
      fun `should save and then update session delivery answers`() {
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

        val checkboxQuestion = actionPlanStepQuestionRepository.save(
          ActionPlanStepQuestionFactory()
            .withActionPlanStepId(sessionDeliveryStep.id)
            .withOrderNumber(2)
            .withTitle("Which communication channels will be used?")
            .withAnswerType(ActionPlanQuestionAnswerType.CHECKBOX)
            .withMaxNumberResponses(3)
            .create(),
        )

        actionPlanStepQuestionChoiceRepository.save(
          ActionPlanStepQuestionChoiceFactory()
            .withActionPlanStepQuestionId(checkboxQuestion.id)
            .withOrderNumber(1)
            .withLabel("Phone")
            .withValue("PHONE")
            .create(),
        )
        actionPlanStepQuestionChoiceRepository.save(
          ActionPlanStepQuestionChoiceFactory()
            .withActionPlanStepQuestionId(checkboxQuestion.id)
            .withOrderNumber(2)
            .withLabel("Text")
            .withValue("TEXT")
            .create(),
        )

        val saveRequest = ActionPlanSessionDeliveryDetailsRequest(
          questions = listOf(
            SessionDeliveryQuestionRequest(
              id = radioQuestion.id,
              savedResponses = listOf(SavedResponse(value = "OTHER", additionalDetails = "Poor weather")),
            ),
            SessionDeliveryQuestionRequest(
              id = checkboxQuestion.id,
              savedResponses = listOf(
                SavedResponse(value = "PHONE"),
                SavedResponse(value = "TEXT"),
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
            body.questions.first { it.id == checkboxQuestion.id }.savedResponses.map { it.value } shouldBe listOf("PHONE", "TEXT")
          }

        val updateRequest = ActionPlanSessionDeliveryDetailsRequest(
          questions = listOf(
            SessionDeliveryQuestionRequest(
              id = radioQuestion.id,
              savedResponses = listOf(SavedResponse(value = "FACE_TO_FACE")),
            ),
            SessionDeliveryQuestionRequest(
              id = checkboxQuestion.id,
              savedResponses = listOf(SavedResponse(value = "TEXT")),
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
            body.questions.first { it.id == checkboxQuestion.id }.savedResponses.map { it.value } shouldBe listOf("TEXT")
          }

        val activeAnswers = actionPlanStepQuestionAnswerRepository.findAllByActionPlanIdAndDeletedAtIsNull(actionPlan.id)
        activeAnswers.filter { it.actionPlanStepQuestionId == radioQuestion.id }.size shouldBe 1
        activeAnswers.filter { it.actionPlanStepQuestionId == checkboxQuestion.id }.size shouldBe 1

        val allAnswers = actionPlanStepQuestionAnswerRepository.findAll()
        allAnswers
          .filter { it.actionPlanId == actionPlan.id && it.actionPlanStepQuestionId == checkboxQuestion.id }
          .count { it.deletedAt != null } shouldBe 1

        val updatedRadioAnswer = activeAnswers.first { it.actionPlanStepQuestionId == radioQuestion.id }
        val radioRevisions = actionPlanStepQuestionAnswerRevisionRepository
          .findAllByActionPlanStepQuestionAnswerIdIn(listOf(updatedRadioAnswer.id))
          .sortedBy { it.revisionNumber }
        radioRevisions.map { it.content } shouldBe listOf("OTHER", "FACE_TO_FACE")
        radioRevisions.map { it.freeTextValue } shouldBe listOf("Poor weather", null)
      }

      @Test
      fun `should return not found for unknown referral reference`() {
        assertNotFound(
          PATCH,
          "/referral/ZZ9999ZZ/action-plan/session-delivery-details",
          ActionPlanSessionDeliveryDetailsRequest(questions = emptyList()),
        )
      }
    }

    private fun createReferral(firstName: String, lastName: String): Referral {
      val user = referralHelper.ensureReferralUser()
      val person = referralHelper.createPerson(firstName = firstName, lastName = lastName)
      return referralHelper.createReferral(person = person, referenceNumber = randomReferralReference(), submittedBy = user)
    }
  }
}
