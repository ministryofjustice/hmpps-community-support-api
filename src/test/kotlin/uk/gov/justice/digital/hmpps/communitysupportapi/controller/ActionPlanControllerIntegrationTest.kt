package uk.gov.justice.digital.hmpps.communitysupportapi.controller

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod.GET
import org.springframework.test.web.reactive.server.expectBody
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.ActionPlanNeedsResponse
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.ActionPlanSessionDeliveryDetailsResponse
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.ActionPlanSummaryDto
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanQuestionAnswerType
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanQuestionType
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanStepQuestion
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanStepQuestionAnswer
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanStepQuestionAnswerRevision
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanStepType
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanTemplate
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
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.ActionPlanActivityFactory
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

  @Autowired
  private lateinit var actionPlanActivityRepository: uk.gov.justice.digital.hmpps.communitysupportapi.repository.ActionPlanActivityRepository

  @Autowired
  private lateinit var actionPlanTemplateRepository: uk.gov.justice.digital.hmpps.communitysupportapi.repository.ActionPlanTemplateRepository

  private fun saveStepQuestionAnswer(
    actionPlanId: UUID,
    actionPlanStepQuestionId: UUID,
    orderNumber: Int,
    createdBy: String,
  ): ActionPlanStepQuestionAnswer = actionPlanStepQuestionAnswerRepository.save(
    ActionPlanStepQuestionAnswer(
      id = UUID.randomUUID(),
      actionPlanId = actionPlanId,
      actionPlanStepQuestionId = actionPlanStepQuestionId,
      orderNumber = orderNumber,
      createdAt = OffsetDateTime.now(),
      createdBy = createdBy,
    ),
  )

  private fun saveStepQuestionAnswerRevision(
    actionPlanStepQuestionAnswerId: UUID,
    revisionNumber: Int,
    content: String,
    freeTextValue: String? = null,
    createdBy: String,
  ) = actionPlanStepQuestionAnswerRevisionRepository.save(
    ActionPlanStepQuestionAnswerRevision(
      id = UUID.randomUUID(),
      actionPlanStepQuestionAnswerId = actionPlanStepQuestionAnswerId,
      revisionNumber = revisionNumber,
      content = content,
      freeTextValue = freeTextValue,
      createdAt = OffsetDateTime.now(),
      createdBy = createdBy,
    ),
  )

  private fun saveActivity(
    actionPlanStepQuestionId: UUID,
    who: String = "SYSTEM",
    activityDetails: String,
    status: String = "OPEN",
  ) = actionPlanActivityRepository.save(
    ActionPlanActivityFactory()
      .withActionPlanStepQuestionId(actionPlanStepQuestionId)
      .withWho(who)
      .withActivityDetails(activityDetails)
      .withStatus(status)
      .create(),
  )

  private fun saveStep(
    actionPlanTemplateId: UUID,
    orderNumber: Int,
    name: String,
    stepType: ActionPlanStepType,
  ) = actionPlanStepRepository.save(
    ActionPlanStepFactory()
      .withActionPlanTemplateId(actionPlanTemplateId)
      .withOrderNumber(orderNumber)
      .withName(name)
      .withStepType(stepType)
      .create(),
  )

  private fun saveStepQuestion(
    actionPlanStepId: UUID,
    orderNumber: Int,
    title: String,
    answerType: ActionPlanQuestionAnswerType = ActionPlanQuestionAnswerType.TEXTAREA,
    questionType: ActionPlanQuestionType = ActionPlanQuestionType.GENERAL,
    maxNumberResponses: Int = 10,
    needId: UUID? = null,
  ): ActionPlanStepQuestion = actionPlanStepQuestionRepository.save(
    ActionPlanStepQuestionFactory()
      .withActionPlanStepId(actionPlanStepId)
      .withOrderNumber(orderNumber)
      .withTitle(title)
      .withAnswerType(answerType)
      .withQuestionType(questionType)
      .withMaxNumberResponses(maxNumberResponses)
      .withNeedId(needId)
      .create(),
  )

  private fun saveStepQuestionChoice(
    actionPlanStepQuestionId: UUID,
    orderNumber: Int,
    label: String,
    value: String,
    hasFreeText: Boolean = false,
    freeTextLabel: String? = null,
  ) = actionPlanStepQuestionChoiceRepository.save(
    ActionPlanStepQuestionChoiceFactory()
      .withActionPlanStepQuestionId(actionPlanStepQuestionId)
      .withOrderNumber(orderNumber)
      .withLabel(label)
      .withValue(value)
      .withHasFreeText(hasFreeText)
      .withFreeTextLabel(freeTextLabel)
      .create(),
  )

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

        val needStep = saveStep(
          actionPlanTemplateId = actionPlanTemplate.id,
          orderNumber = 1,
          name = "Needs",
          stepType = ActionPlanStepType.NEED,
        )

        saveStepQuestion(
          actionPlanStepId = needStep.id,
          orderNumber = 1,
          title = "Question for second need",
          needId = secondNeed.id,
        )
        saveStepQuestion(
          actionPlanStepId = needStep.id,
          orderNumber = 2,
          title = "First question for first need",
          needId = firstNeed.id,
        )
        saveStepQuestion(
          actionPlanStepId = needStep.id,
          orderNumber = 3,
          title = "Second question for first need",
          needId = firstNeed.id,
        )
        saveStepQuestion(
          actionPlanStepId = needStep.id,
          orderNumber = 4,
          title = "Question without need",
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

        val sessionDeliveryStep = saveStep(
          actionPlanTemplateId = actionPlanTemplate.id,
          orderNumber = 2,
          name = "Service Delivery Details",
          stepType = ActionPlanStepType.SESSION_DELIVERY,
        )

        val question1 = saveStepQuestion(
          actionPlanStepId = sessionDeliveryStep.id,
          orderNumber = 1,
          title = "How will the session be delivered?",
          answerType = ActionPlanQuestionAnswerType.RADIO,
          maxNumberResponses = 1,
        )

        saveStepQuestionChoice(
          actionPlanStepQuestionId = question1.id,
          orderNumber = 1,
          label = "Face-to-face",
          value = "FACE_TO_FACE",
        )
        saveStepQuestionChoice(
          actionPlanStepQuestionId = question1.id,
          orderNumber = 2,
          label = "Other",
          value = "OTHER",
          hasFreeText = true,
          freeTextLabel = "Reason for not meeting face-to-face",
        )

        val question2 = saveStepQuestion(
          actionPlanStepId = sessionDeliveryStep.id,
          orderNumber = 2,
          title = "How many sessions are required?",
          maxNumberResponses = 1,
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

        val sessionDeliveryStep = saveStep(
          actionPlanTemplateId = actionPlanTemplate.id,
          orderNumber = 2,
          name = "Service Delivery Details",
          stepType = ActionPlanStepType.SESSION_DELIVERY,
        )

        val question = saveStepQuestion(
          actionPlanStepId = sessionDeliveryStep.id,
          orderNumber = 1,
          title = "How many people will be in the session?",
          answerType = ActionPlanQuestionAnswerType.RADIO,
          maxNumberResponses = 1,
        )

        saveStepQuestionChoice(
          actionPlanStepQuestionId = question.id,
          orderNumber = 1,
          label = "One-to-one",
          value = "ONE_TO_ONE",
        )
        saveStepQuestionChoice(
          actionPlanStepQuestionId = question.id,
          orderNumber = 2,
          label = "In a group",
          value = "IN_A_GROUP",
          hasFreeText = true,
          freeTextLabel = "How many people will be in the group?",
        )

        val answerId = saveStepQuestionAnswer(
          actionPlanId = actionPlan.id,
          actionPlanStepQuestionId = question.id,
          orderNumber = 1,
          createdBy = user.id.toString(),
        ).id

        saveStepQuestionAnswerRevision(
          actionPlanStepQuestionAnswerId = answerId,
          revisionNumber = 1,
          content = "IN_A_GROUP",
          freeTextValue = "3 people",
          createdBy = user.id.toString(),
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

        val sessionDeliveryStep = saveStep(
          actionPlanTemplateId = actionPlanTemplate.id,
          orderNumber = 2,
          name = "Service Delivery Details",
          stepType = ActionPlanStepType.SESSION_DELIVERY,
        )

        val question = saveStepQuestion(
          actionPlanStepId = sessionDeliveryStep.id,
          orderNumber = 1,
          title = "What communication methods will be used?",
          answerType = ActionPlanQuestionAnswerType.CHECKBOX,
          maxNumberResponses = 3,
        )

        saveStepQuestionChoice(
          actionPlanStepQuestionId = question.id,
          orderNumber = 1,
          label = "By phone",
          value = "BY_PHONE",
        )
        saveStepQuestionChoice(
          actionPlanStepQuestionId = question.id,
          orderNumber = 2,
          label = "By message",
          value = "BY_MESSAGE",
        )
        saveStepQuestionChoice(
          actionPlanStepQuestionId = question.id,
          orderNumber = 3,
          label = "By email",
          value = "BY_EMAIL",
        )

        val firstAnswerId = saveStepQuestionAnswer(
          actionPlanId = actionPlan.id,
          actionPlanStepQuestionId = question.id,
          orderNumber = 1,
          createdBy = user.id.toString(),
        ).id
        val secondAnswerId = saveStepQuestionAnswer(
          actionPlanId = actionPlan.id,
          actionPlanStepQuestionId = question.id,
          orderNumber = 2,
          createdBy = user.id.toString(),
        ).id
        val thirdAnswerId = saveStepQuestionAnswer(
          actionPlanId = actionPlan.id,
          actionPlanStepQuestionId = question.id,
          orderNumber = 3,
          createdBy = user.id.toString(),
        ).id

        saveStepQuestionAnswerRevision(
          actionPlanStepQuestionAnswerId = firstAnswerId,
          revisionNumber = 1,
          content = "BY_PHONE",
          createdBy = user.id.toString(),
        )
        saveStepQuestionAnswerRevision(
          actionPlanStepQuestionAnswerId = secondAnswerId,
          revisionNumber = 1,
          content = "BY_MESSAGE",
          createdBy = user.id.toString(),
        )
        saveStepQuestionAnswerRevision(
          actionPlanStepQuestionAnswerId = thirdAnswerId,
          revisionNumber = 1,
          content = "BY_EMAIL",
          createdBy = user.id.toString(),
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

    private fun createReferral(firstName: String, lastName: String): Referral {
      val user = referralHelper.ensureReferralUser()
      val person = referralHelper.createPerson(firstName = firstName, lastName = lastName)
      return referralHelper.createReferral(person = person, referenceNumber = randomReferralReference(), submittedBy = user)
    }
  }

  @Nested
  @DisplayName("POST /bff/referral/{referralReference}/action-plan/submit")
  inner class SubmitActionPlanEndpoint {

    @Test
    fun `should return unauthorized if no token`() {
      assertUnauthorized(org.springframework.http.HttpMethod.POST, "/bff/referral/AB1234CD/action-plan/submit")
    }

    @Test
    fun `should return forbidden if no role`() {
      assertForbiddenNoRole(org.springframework.http.HttpMethod.POST, "/bff/referral/AB1234CD/action-plan/submit")
    }

    @Test
    fun `should return forbidden if wrong role`() {
      assertForbiddenWrongRole(org.springframework.http.HttpMethod.POST, "/bff/referral/AB1234CD/action-plan/submit")
    }

    @Test
    fun `should return OK when all outcome questions with choices have at least one activity`() {
      val user = referralHelper.ensureReferralUser()
      val person = referralHelper.createPerson()
      val referral = referralHelper.createReferral(
        person = person,
        referenceNumber = randomReferralReference(),
        submittedBy = user,
      )
      val (template, outcomeQuestion) = createTemplateWithOutcomeQuestionAndChoice()
      actionPlanHelper.createActionPlan(referralId = referral.id, templateId = template.id)
      saveActivity(
        actionPlanStepQuestionId = outcomeQuestion.id,
        activityDetails = "Activity 1",
      )

      webTestClient.post()
        .uri("/bff/referral/${referral.referenceNumber}/action-plan/submit")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus().isOk
    }

    @Test
    fun `should return bad request when an outcome question with choices has no activity`() {
      val user = referralHelper.ensureReferralUser()
      val person = referralHelper.createPerson()
      val referral = referralHelper.createReferral(
        person = person,
        referenceNumber = randomReferralReference(),
        submittedBy = user,
      )
      val (template) = createTemplateWithOutcomeQuestionAndChoice()
      actionPlanHelper.createActionPlan(referralId = referral.id, templateId = template.id)

      webTestClient.post()
        .uri("/bff/referral/${referral.referenceNumber}/action-plan/submit")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus().isBadRequest
    }

    @Test
    fun `should return OK when outcome questions exist but have no choices`() {
      val user = referralHelper.ensureReferralUser()
      val person = referralHelper.createPerson()
      val referral = referralHelper.createReferral(
        person = person,
        referenceNumber = randomReferralReference(),
        submittedBy = user,
      )
      val globalTemplate = actionPlanTemplateRepository.getGlobalActionPlanTemplate()!!
      actionPlanHelper.createActionPlan(referralId = referral.id, templateId = globalTemplate.id)

      webTestClient.post()
        .uri("/bff/referral/${referral.referenceNumber}/action-plan/submit")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus().isOk
    }

    @Test
    fun `should return 409 conflict when action plan has already been submitted`() {
      val user = referralHelper.ensureReferralUser()
      val person = referralHelper.createPerson()
      val referral = referralHelper.createReferral(
        person = person,
        referenceNumber = randomReferralReference(),
        submittedBy = user,
      )
      val globalTemplate = actionPlanTemplateRepository.getGlobalActionPlanTemplate()!!
      actionPlanHelper.createSubmittedActionPlan(referralId = referral.id, templateId = globalTemplate.id)

      webTestClient.post()
        .uri("/bff/referral/${referral.referenceNumber}/action-plan/submit")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus().isEqualTo(409)
    }

    private fun createTemplateWithOutcomeQuestionAndChoice(): Pair<ActionPlanTemplate, ActionPlanStepQuestion> {
      val template = actionPlanHelper.createActionPlanTemplate()
      val step = saveStep(
        actionPlanTemplateId = template.id,
        orderNumber = 1,
        name = "Outcomes Step",
        stepType = ActionPlanStepType.NEED,
      )
      val question = saveStepQuestion(
        actionPlanStepId = step.id,
        orderNumber = 1,
        title = "What is the desired outcome?",
        answerType = ActionPlanQuestionAnswerType.RADIO,
        questionType = ActionPlanQuestionType.OUTCOME,
      )
      saveStepQuestionChoice(
        actionPlanStepQuestionId = question.id,
        orderNumber = 1,
        label = "Option A",
        value = "OPTION_A",
      )
      return Pair(template, question)
    }
  }
}
