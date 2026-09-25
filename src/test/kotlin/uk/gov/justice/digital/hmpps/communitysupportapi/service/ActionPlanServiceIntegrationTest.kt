package uk.gov.justice.digital.hmpps.communitysupportapi.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.ActionPlanSessionDeliveryDetailsRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.SessionDeliveryDetailsQuestionAnswer
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.SessionDeliveryDetailsQuestionAnswers
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlan
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanQuestionAnswerType
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanQuestionResponseEventType
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanQuestionType
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanStep
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanStepQuestion
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanStepQuestionAnswerDetails
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanStepQuestionAnswerHeader
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanStepType
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Referral
import uk.gov.justice.digital.hmpps.communitysupportapi.exception.NotFoundException
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.ActionPlanTestSupport
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.ReferralTestSupport
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ActionPlanEventRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ActionPlanQuestionResponseEventRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ActionPlanRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ActionPlanStepQuestionAnswerDetailsRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ActionPlanStepQuestionAnswerHeaderRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ActionPlanStepQuestionChoiceRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ActionPlanStepQuestionRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ActionPlanStepRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ActionPlanTemplateRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.NeedRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.ActionPlanStepFactory
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.ActionPlanStepQuestionAnswerDetailsFactory
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.ActionPlanStepQuestionAnswerHeaderFactory
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.ActionPlanStepQuestionChoiceFactory
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.ActionPlanStepQuestionFactory
import uk.gov.justice.digital.hmpps.communitysupportapi.util.ReferralReferenceTestUtil.randomReferralReference
import java.time.OffsetDateTime
import java.util.UUID

class ActionPlanServiceIntegrationTest :
  IntegrationTestBase(),
  AfterAllCallback {

  @Autowired
  private lateinit var actionPlanService: ActionPlanService

  @Autowired
  private lateinit var referralHelper: ReferralTestSupport

  @Autowired
  private lateinit var actionPlanHelper: ActionPlanTestSupport

  @Autowired
  private lateinit var actionPlanTemplateRepository: ActionPlanTemplateRepository

  @Autowired
  private lateinit var actionPlanEventRepository: ActionPlanEventRepository

  @Autowired
  private lateinit var actionPlanQuestionResponseEventRepository: ActionPlanQuestionResponseEventRepository

  @Autowired
  private lateinit var actionPlanRepository: ActionPlanRepository

  @Autowired
  private lateinit var needRepository: NeedRepository

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

  override fun afterAll(context: ExtensionContext) {
    testDataCleaner.cleanAllTables()
  }

  @Nested
  @DisplayName("findOrCreateByReferralId")
  inner class FindOrCreateByReferralId {
    val user = referralHelper.ensureReferralUser()
    val globalTemplate = actionPlanTemplateRepository.getGlobalActionPlanTemplate()
      ?: throw NotFoundException("Cannot find Global ActionPlan")

    @Test
    fun `should not create an additional ActionPlan when one already exists`() {
      // Given
      val referral = referralHelper.createReferral(submittedBy = user)
      val existingActionPlan = actionPlanHelper.createActionPlan(
        referralId = referral.id,
        templateId = globalTemplate.id,
        createdAt = OffsetDateTime.now(),
        updatedAt = OffsetDateTime.now(),
      )

      // When
      val result = actionPlanService.findOrCreateByReferralId(referral.id)

      // Then
      val allActionPlansForReferral = actionPlanRepository.findAllByReferralId(referral.id)
      assertEquals(existingActionPlan.id, result.id)
      assertEquals(allActionPlansForReferral.size, 1)
    }

    @Test
    fun `should create an ActionPlan when one does not exists`() {
      // Given
      val referral = referralHelper.createReferral(submittedBy = user)

      // When
      assertEquals(actionPlanRepository.findAllByReferralId(referral.id).size, 0)
      val result = actionPlanService.findOrCreateByReferralId(referral.id)

      // Then
      val allActionPlans = actionPlanRepository.findAllByReferralId(referral.id)
      assertEquals(result.referralId, referral.id)
      assertEquals(globalTemplate.id, result.actionPlanTemplateId)
      assertEquals(allActionPlans.size, 1)
    }

    @Test
    fun `should create an ActionPlan using active global template when lower non-global template exists`() {
      // Given
      val referral = referralHelper.createReferral(submittedBy = user)
      assertTrue(actionPlanTemplateRepository.getGlobalActionPlanTemplate() != null)
      actionPlanHelper.createActionPlanTemplate(
        id = UUID.fromString("00000000-0000-0000-0000-000000000001"),
        activeGlobal = false,
      )

      // When
      val result = actionPlanService.findOrCreateByReferralId(referral.id)

      // Then
      assertEquals(globalTemplate.id, result.actionPlanTemplateId)
    }
  }

  @Nested
  @DisplayName("getActionPlanSummaryForReferral")
  inner class GetActionPlanSummaryForReferral {
    val user = referralHelper.ensureReferralUser()
    val globalTemplate = actionPlanTemplateRepository.getGlobalActionPlanTemplate()
      ?: throw NotFoundException("Cannot find Global ActionPlan")

    @Test
    fun `should return person details for a referral`() {
      // Given
      val person = referralHelper.createPerson(firstName = "Adam", lastName = "Smith")
      val referral =
        referralHelper.createReferral(person = person, referenceNumber = randomReferralReference(), submittedBy = user)

      // When
      val result = actionPlanService.getActionPlanSummaryForReferral(referral.referenceNumber!!)

      // Then
      assertEquals("Adam", result.personDetails.firstName)
      assertEquals("Smith", result.personDetails.lastName)
      assertEquals(0, result.needs.size)
    }

    @Test
    fun `should return latest revision content for an outcome answer`() {
      // Given
      val person = referralHelper.createPerson(firstName = "Jane", lastName = "Doe")
      val referral =
        referralHelper.createReferral(person = person, referenceNumber = randomReferralReference(), submittedBy = user)
      val actionPlan = actionPlanHelper.createActionPlan(referralId = referral.id, templateId = globalTemplate.id)
      val need = needRepository.findAllByOrderByOrderNumberAsc().first()
      val outcomeQuestion = findOutcomeQuestionForNeed(globalTemplate.id, need.id)

      val answer = actionPlanHelper.createActionPlanStepQuestionAnswerHeader(actionPlan.id, outcomeQuestion.id, createdBy = user.id.toString())
      actionPlanHelper.createActionPlanStepQuestionAnswerDetails(answer.id, revisionNumber = 1, content = "Initial wording", createdBy = user.id.toString())
      actionPlanHelper.createActionPlanStepQuestionAnswerDetails(answer.id, revisionNumber = 2, content = "Final wording", createdBy = user.id.toString())
      actionPlanHelper.createActionPlanActivity(answer.id)

      // When
      val result = actionPlanService.getActionPlanSummaryForReferral(referral.referenceNumber!!)

      // Then
      val needSummary = result.needs.first { it.id == need.id }
      assertEquals(listOf("Final wording"), needSummary.outcomes.map { it.label })
    }

    @Test
    fun `should return multiple outcome answers in order for the same need`() {
      // Given
      val person = referralHelper.createPerson(firstName = "Ella", lastName = "Brown")
      val referral =
        referralHelper.createReferral(person = person, referenceNumber = randomReferralReference(), submittedBy = user)
      val actionPlan = actionPlanHelper.createActionPlan(referralId = referral.id, templateId = globalTemplate.id)
      val need = needRepository.findAllByOrderByOrderNumberAsc().first()
      val outcomeQuestion = findOutcomeQuestionForNeed(globalTemplate.id, need.id)

      val firstAnswer = actionPlanHelper.createActionPlanStepQuestionAnswerHeader(actionPlan.id, outcomeQuestion.id, orderNumber = 1, createdBy = user.id.toString())
      val secondAnswer = actionPlanHelper.createActionPlanStepQuestionAnswerHeader(actionPlan.id, outcomeQuestion.id, orderNumber = 2, createdBy = user.id.toString())
      actionPlanHelper.createActionPlanStepQuestionAnswerDetails(firstAnswer.id, content = "First outcome", createdBy = user.id.toString())
      actionPlanHelper.createActionPlanStepQuestionAnswerDetails(secondAnswer.id, content = "Second outcome", createdBy = user.id.toString())
      actionPlanHelper.createActionPlanActivity(firstAnswer.id)
      actionPlanHelper.createActionPlanActivity(secondAnswer.id)

      // When
      val result = actionPlanService.getActionPlanSummaryForReferral(referral.referenceNumber!!)

      // Then
      val needSummary = result.needs.first { it.id == need.id }
      assertEquals(listOf("First outcome", "Second outcome"), needSummary.outcomes.map { it.label })
    }

    @Test
    fun `should ignore soft deleted answers when building outcomes`() {
      // Given
      val person = referralHelper.createPerson(firstName = "Sam", lastName = "Green")
      val referral =
        referralHelper.createReferral(person = person, referenceNumber = randomReferralReference(), submittedBy = user)
      val actionPlan = actionPlanHelper.createActionPlan(referralId = referral.id, templateId = globalTemplate.id)
      val need = needRepository.findAllByOrderByOrderNumberAsc().first()
      val outcomeQuestion = findOutcomeQuestionForNeed(globalTemplate.id, need.id)

      val activeAnswer = actionPlanHelper.createActionPlanStepQuestionAnswerHeader(actionPlan.id, outcomeQuestion.id, createdBy = user.id.toString())
      val deletedAnswer = actionPlanHelper.createActionPlanStepQuestionAnswerHeader(
        actionPlan.id,
        outcomeQuestion.id,
        orderNumber = 2,
        createdBy = user.id.toString(),
        deletedAt = OffsetDateTime.now(),
        deletedBy = user.id.toString(),
      )
      actionPlanHelper.createActionPlanStepQuestionAnswerDetails(activeAnswer.id, content = "Visible outcome", createdBy = user.id.toString())
      actionPlanHelper.createActionPlanStepQuestionAnswerDetails(deletedAnswer.id, content = "Hidden outcome", createdBy = user.id.toString())
      actionPlanHelper.createActionPlanActivity(activeAnswer.id)
      actionPlanHelper.createActionPlanActivity(deletedAnswer.id)

      // When
      val result = actionPlanService.getActionPlanSummaryForReferral(referral.referenceNumber!!)

      // Then
      val needSummary = result.needs.first { it.id == need.id }
      assertEquals(listOf("Visible outcome"), needSummary.outcomes.map { it.label })
    }

    private fun findOutcomeQuestionForNeed(templateId: UUID, needId: UUID): ActionPlanStepQuestion {
      val needSteps = actionPlanStepRepository
        .findAllByActionPlanTemplateIdOrderByOrderNumberAsc(templateId)
        .filter { it.stepType == ActionPlanStepType.NEED }
      return actionPlanStepQuestionRepository
        .findAllByActionPlanStepIdInOrderByOrderNumberAsc(needSteps.map { it.id })
        .first { it.questionType == ActionPlanQuestionType.OUTCOME && it.needId == needId }
    }
  }

  @Nested
  @DisplayName("getMostRecentResponseToQuestionForActionPlan")
  inner class GetMostRecentResponseToQuestionForActionPlan {
    val user = referralHelper.ensureReferralUser()
    val globalTemplate = actionPlanTemplateRepository.getGlobalActionPlanTemplate()
      ?: throw NotFoundException("Cannot find Global ActionPlan")

    @Test
    fun `should return the latest details for an active answer to the question`() {
      val person = referralHelper.createPerson(firstName = "Chris", lastName = "Taylor")
      val referral =
        referralHelper.createReferral(person = person, referenceNumber = randomReferralReference(), submittedBy = user)
      val actionPlan = actionPlanHelper.createActionPlan(referralId = referral.id, templateId = globalTemplate.id)
      val question = actionPlanStepQuestionRepository.findAll().first()
      val now = OffsetDateTime.now()

      val olderHeader = actionPlanStepQuestionAnswerHeaderRepository.save(
        ActionPlanStepQuestionAnswerHeader(
          id = UUID.randomUUID(),
          actionPlanId = actionPlan.id,
          actionPlanStepQuestionId = question.id,
          orderNumber = 1,
          createdAt = now.minusMinutes(3),
          createdBy = user.id.toString(),
        ),
      )
      val latestHeader = actionPlanStepQuestionAnswerHeaderRepository.save(
        ActionPlanStepQuestionAnswerHeader(
          id = UUID.randomUUID(),
          actionPlanId = actionPlan.id,
          actionPlanStepQuestionId = question.id,
          orderNumber = 2,
          createdAt = now.minusMinutes(2),
          createdBy = user.id.toString(),
        ),
      )
      val deletedHeader = actionPlanStepQuestionAnswerHeaderRepository.save(
        ActionPlanStepQuestionAnswerHeader(
          id = UUID.randomUUID(),
          actionPlanId = actionPlan.id,
          actionPlanStepQuestionId = question.id,
          orderNumber = 3,
          createdAt = now.minusMinutes(1),
          createdBy = user.id.toString(),
          deletedAt = now,
          deletedBy = user.id.toString(),
        ),
      )

      actionPlanStepQuestionAnswerDetailsRepository.save(
        ActionPlanStepQuestionAnswerDetails(
          id = UUID.randomUUID(),
          actionPlanStepQuestionAnswerHeaderId = olderHeader.id,
          revisionNumber = 1,
          content = "Older response",
          createdAt = now.minusMinutes(2),
          createdBy = user.id.toString(),
        ),
      )
      val expectedDetails = actionPlanStepQuestionAnswerDetailsRepository.save(
        ActionPlanStepQuestionAnswerDetails(
          id = UUID.randomUUID(),
          actionPlanStepQuestionAnswerHeaderId = latestHeader.id,
          revisionNumber = 1,
          content = "Latest response",
          createdAt = now.minusMinutes(1),
          createdBy = user.id.toString(),
        ),
      )
      actionPlanStepQuestionAnswerDetailsRepository.save(
        ActionPlanStepQuestionAnswerDetails(
          id = UUID.randomUUID(),
          actionPlanStepQuestionAnswerHeaderId = deletedHeader.id,
          revisionNumber = 1,
          content = "Deleted response",
          createdAt = now,
          createdBy = user.id.toString(),
        ),
      )

      val answers = actionPlanStepQuestionAnswerDetailsRepository
        .getMostRecentAnswersForActionPlanQuestion(question.id, actionPlan.id)

      assertEquals(listOf(expectedDetails.id), answers.map { it.id })
    }
  }

  @Nested
  @DisplayName("session delivery details")
  inner class SessionDeliveryDetails {
    val user = referralHelper.ensureReferralUser()
    private lateinit var referral: Referral
    private lateinit var actionPlan: ActionPlan
    private lateinit var sessionDeliveryStep: ActionPlanStep

    @BeforeEach
    fun setUpSessionDeliveryDetails() {
      val person = referralHelper.createPerson(firstName = "Jane", lastName = "Doe")
      referral =
        referralHelper.createReferral(person = person, referenceNumber = randomReferralReference(), submittedBy = user)
      val actionPlanTemplate = actionPlanHelper.createActionPlanTemplate()
      actionPlan = actionPlanHelper.createActionPlan(referralId = referral.id, templateId = actionPlanTemplate.id)
      sessionDeliveryStep = createSessionDeliveryStep(actionPlanTemplate.id)
    }

    @Test
    fun `should return saved responses and choices ordered by display order`() {
      val question = createSessionDeliveryQuestion(1, "How will the session be delivered?")

      createChoice(question, 1, "Face-to-face", "FACE_TO_FACE")
      createChoice(
        question,
        2,
        "Other",
        "OTHER",
        hasFreeText = true,
        freeTextLabel = "Reason for not meeting face-to-face",
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
          content = "OTHER",
          freeTextValue = "Poor weather",
          createdAt = OffsetDateTime.now(),
          createdBy = user.id.toString(),
        ),
      )

      val result = actionPlanService.getSessionDeliveryDetailsForReferral(referral.referenceNumber!!)
      val returnedQuestion = result.questions.single()

      assertEquals(question.id, returnedQuestion.id)
      assertEquals(question.questionKey, returnedQuestion.key)
      val choices = returnedQuestion.choices ?: error("Expected choices for session delivery question")
      assertEquals(listOf("FACE_TO_FACE", "OTHER"), choices.map { it.value })
      assertEquals(listOf("Face-to-face", "Other"), choices.map { it.label })
      assertEquals(listOf("OTHER"), returnedQuestion.savedResponses.map { it.value })
      assertEquals(listOf("Poor weather"), returnedQuestion.savedResponses.map { it.additionalDetails })
    }

    @Test
    fun `should save, update, and soft delete session delivery answers`() {
      val radioQuestion = createSessionDeliveryQuestion(1, "How will the session be delivered?")
      createChoice(radioQuestion, 1, "Face-to-face", "FACE_TO_FACE")
      createChoice(radioQuestion, 2, "Other", "OTHER", hasFreeText = true, freeTextLabel = "Reason")

      val secondQuestion = createSessionDeliveryQuestion(2, "Which support is needed?")
      createChoice(secondQuestion, 1, "Short session", "SHORT_SESSION")
      createChoice(secondQuestion, 2, "Long session", "LONG_SESSION")

      val saveRequest = sessionDeliveryDetailsRequest(
        radioQuestion to listOf(sessionDeliveryDetailsAnswer("OTHER", "Poor weather")),
        secondQuestion to listOf(sessionDeliveryDetailsAnswer("SHORT_SESSION")),
      )

      val saveResult = updateSessionDeliveryDetails(saveRequest)
      assertEquals(
        listOf("OTHER"),
        saveResult.questions.first { it.id == radioQuestion.id }.savedResponses.map { it.value },
      )
      assertEquals(
        radioQuestion.questionKey,
        saveResult.questions.first { it.id == radioQuestion.id }.key,
      )
      assertEquals(
        listOf("Poor weather"),
        saveResult.questions.first { it.id == radioQuestion.id }.savedResponses.map { it.additionalDetails },
      )
      assertEquals(
        listOf("SHORT_SESSION"),
        saveResult.questions.first { it.id == secondQuestion.id }.savedResponses.map { it.value },
      )

      val updateRequest = sessionDeliveryDetailsRequest(
        radioQuestion to listOf(sessionDeliveryDetailsAnswer("FACE_TO_FACE")),
        secondQuestion to emptyList(),
      )

      val updateResult = updateSessionDeliveryDetails(updateRequest)
      assertEquals(
        listOf("FACE_TO_FACE"),
        updateResult.questions.first { it.id == radioQuestion.id }.savedResponses.map { it.value },
      )
      assertEquals(
        radioQuestion.questionKey,
        updateResult.questions.first { it.id == radioQuestion.id }.key,
      )
      assertEquals(
        listOf(null),
        updateResult.questions.first { it.id == radioQuestion.id }.savedResponses.map { it.additionalDetails },
      )
      assertTrue(updateResult.questions.first { it.id == secondQuestion.id }.savedResponses.isEmpty())

      val activeAnswers =
        actionPlanStepQuestionAnswerHeaderRepository.findAllByActionPlanIdAndDeletedAtIsNull(actionPlan.id)
      assertEquals(1, activeAnswers.count { it.actionPlanStepQuestionId == radioQuestion.id })
      assertEquals(0, activeAnswers.count { it.actionPlanStepQuestionId == secondQuestion.id })
      assertEquals(
        1,
        actionPlanStepQuestionAnswerHeaderRepository.findAll().count {
          it.actionPlanId == actionPlan.id &&
            it.actionPlanStepQuestionId == secondQuestion.id &&
            it.deletedAt != null
        },
      )

      val radioAnswer = activeAnswers.first { it.actionPlanStepQuestionId == radioQuestion.id }
      val radioRevisions = actionPlanStepQuestionAnswerDetailsRepository
        .findAllByActionPlanStepQuestionAnswerHeaderIdIn(listOf(radioAnswer.id))
        .sortedBy { it.revisionNumber }
      assertEquals(listOf("OTHER", "FACE_TO_FACE"), radioRevisions.map { it.content })
      assertEquals(listOf("Poor weather", null), radioRevisions.map { it.freeTextValue })

      val questionResponseEvents = actionPlanQuestionResponseEventRepository.findByActionPlanId(actionPlan.id)
      assertEquals(4, questionResponseEvents.size)
      assertEquals(2, questionResponseEvents.count { it.eventType == ActionPlanQuestionResponseEventType.CREATED })
      assertEquals(1, questionResponseEvents.count { it.eventType == ActionPlanQuestionResponseEventType.UPDATED })
      assertEquals(1, questionResponseEvents.count { it.eventType == ActionPlanQuestionResponseEventType.DELETED })
      assertTrue(questionResponseEvents.all { it.questionResponseChangeBatchId != null })
      assertEquals(2, questionResponseEvents.mapNotNull { it.questionResponseChangeBatchId }.distinct().size)

      val radioEvents = questionResponseEvents.filter { it.actionPlanStepQuestionAnswerHeaderId == radioAnswer.id }
        .sortedBy { it.createdAt }
      assertEquals(
        listOf(ActionPlanQuestionResponseEventType.CREATED, ActionPlanQuestionResponseEventType.UPDATED),
        radioEvents.map { it.eventType },
      )

      val deletedHeader = actionPlanStepQuestionAnswerHeaderRepository.findAll().single {
        it.actionPlanId == actionPlan.id &&
          it.actionPlanStepQuestionId == secondQuestion.id &&
          it.deletedAt != null
      }
      val deletedEvents = questionResponseEvents.filter { it.actionPlanStepQuestionAnswerHeaderId == deletedHeader.id }
        .sortedBy { it.createdAt }
      assertEquals(
        listOf(ActionPlanQuestionResponseEventType.CREATED, ActionPlanQuestionResponseEventType.DELETED),
        deletedEvents.map { it.eventType },
      )
    }

    @Test
    fun `should support date answers`() {
      val dateQuestion = createSessionDeliveryQuestion(
        1,
        "What is the new service end date?",
        ActionPlanQuestionAnswerType.DATE,
        1,
      )

      val saveRequest = ActionPlanSessionDeliveryDetailsRequest(
        answers = listOf(
          SessionDeliveryDetailsQuestionAnswers(
            questionId = dateQuestion.id,
            incomingAnswerDetails = listOf(
              SessionDeliveryDetailsQuestionAnswer(value = "2026-10-01"),
            ),
          ),
        ),
      )

      val saveResult = actionPlanService.updateSessionDeliveryDetailsForActionPlan(
        referral.referenceNumber!!,
        saveRequest,
        user.id.toString(),
      )
      assertEquals(
        listOf("2026-10-01"),
        saveResult.questions.single { it.id == dateQuestion.id }.savedResponses.map { it.value },
      )

      val activeHeaders =
        actionPlanStepQuestionAnswerHeaderRepository.findAllByActionPlanIdAndDeletedAtIsNull(actionPlan.id)
      assertEquals(1, activeHeaders.count { it.actionPlanStepQuestionId == dateQuestion.id })
    }

    @Test
    fun `should support multiple selected checkbox answers using one header per selected option`() {
      val checkboxQuestion = createSessionDeliveryQuestion(
        orderNumber = 1,
        title = "Which of these are available?",
        answerType = ActionPlanQuestionAnswerType.CHECKBOX,
        maxNumberResponses = 3,
      )
      createChoice(checkboxQuestion, 1, "In person", "IN_PERSON")
      createChoice(checkboxQuestion, 2, "By phone", "PHONE")
      createChoice(checkboxQuestion, 3, "By video call", "VIDEO")

      val initialRequest = sessionDeliveryDetailsRequest(
        checkboxQuestion to listOf(
          sessionDeliveryDetailsAnswer("IN_PERSON"),
          sessionDeliveryDetailsAnswer("PHONE"),
        ),
      )

      updateSessionDeliveryDetails(initialRequest)

      val updateRequest = sessionDeliveryDetailsRequest(
        checkboxQuestion to listOf(
          sessionDeliveryDetailsAnswer("IN_PERSON"),
          sessionDeliveryDetailsAnswer("VIDEO"),
        ),
      )

      val updateResult = updateSessionDeliveryDetails(updateRequest)
      assertEquals(
        listOf("IN_PERSON", "VIDEO"),
        updateResult.questions.single { it.id == checkboxQuestion.id }.savedResponses.map { it.value },
      )
      assertEquals(
        checkboxQuestion.questionKey,
        updateResult.questions.single { it.id == checkboxQuestion.id }.key,
      )

      val activeHeaders =
        actionPlanStepQuestionAnswerHeaderRepository.findAllByActionPlanIdAndDeletedAtIsNull(actionPlan.id)
          .filter { it.actionPlanStepQuestionId == checkboxQuestion.id }
      assertEquals(2, activeHeaders.size)
      assertEquals(
        setOf("IN_PERSON", "VIDEO"),
        activeHeaders.map { header ->
          actionPlanStepQuestionAnswerDetailsRepository.findAllByActionPlanStepQuestionAnswerHeaderIdIn(listOf(header.id))
            .maxByOrNull { it.revisionNumber }
            ?.content
        }.toSet(),
      )

      val deletedHeaders = actionPlanStepQuestionAnswerHeaderRepository.findAll().filter {
        it.actionPlanId == actionPlan.id &&
          it.actionPlanStepQuestionId == checkboxQuestion.id &&
          it.deletedAt != null
      }
      assertEquals(1, deletedHeaders.size)
      assertEquals(
        listOf("PHONE"),
        deletedHeaders.flatMap { header ->
          actionPlanStepQuestionAnswerDetailsRepository.findAllByActionPlanStepQuestionAnswerHeaderIdIn(listOf(header.id))
            .map { it.content }
        },
      )

      val inPersonHeader = activeHeaders.single { header ->
        actionPlanStepQuestionAnswerDetailsRepository.findAllByActionPlanStepQuestionAnswerHeaderIdIn(listOf(header.id))
          .maxByOrNull { it.revisionNumber }
          ?.content == "IN_PERSON"
      }
      val inPersonRevisions = actionPlanStepQuestionAnswerDetailsRepository
        .findAllByActionPlanStepQuestionAnswerHeaderIdIn(listOf(inPersonHeader.id))
        .sortedBy { it.revisionNumber }
      assertEquals(listOf("IN_PERSON"), inPersonRevisions.map { it.content })

      val questionResponseEvents = actionPlanQuestionResponseEventRepository.findByActionPlanId(actionPlan.id)
      assertEquals(4, questionResponseEvents.size)
      assertEquals(3, questionResponseEvents.count { it.eventType == ActionPlanQuestionResponseEventType.CREATED })
      assertEquals(0, questionResponseEvents.count { it.eventType == ActionPlanQuestionResponseEventType.UPDATED })
      assertEquals(1, questionResponseEvents.count { it.eventType == ActionPlanQuestionResponseEventType.DELETED })
      assertTrue(questionResponseEvents.all { it.questionResponseChangeBatchId != null })
      assertEquals(
        listOf(2, 2),
        questionResponseEvents
          .mapNotNull { it.questionResponseChangeBatchId }
          .groupingBy { it }
          .eachCount()
          .values
          .sorted(),
      )

      val eventTypesByHeaderId = questionResponseEvents.groupBy { it.actionPlanStepQuestionAnswerHeaderId }
      assertEquals(
        listOf(ActionPlanQuestionResponseEventType.CREATED),
        eventTypesByHeaderId[inPersonHeader.id]!!.sortedBy { it.createdAt }.map { it.eventType },
      )
      val videoHeader = activeHeaders.single { header ->
        actionPlanStepQuestionAnswerDetailsRepository.findAllByActionPlanStepQuestionAnswerHeaderIdIn(listOf(header.id))
          .maxByOrNull { it.revisionNumber }
          ?.content == "VIDEO"
      }
      assertEquals(
        listOf(ActionPlanQuestionResponseEventType.CREATED),
        eventTypesByHeaderId[videoHeader.id]!!.sortedBy { it.createdAt }.map { it.eventType },
      )
      assertEquals(
        listOf(ActionPlanQuestionResponseEventType.CREATED, ActionPlanQuestionResponseEventType.DELETED),
        eventTypesByHeaderId[deletedHeaders.single().id]!!.sortedBy { it.createdAt }.map { it.eventType },
      )
    }

    @Test
    fun `should return risk and adjustments questions with saved responses and keys`() {
      val referral = createReferral("Jane", "Doe")
      val actionPlanTemplate = actionPlanHelper.createActionPlanTemplate()
      val actionPlan = actionPlanHelper.createActionPlan(referralId = referral.id, templateId = actionPlanTemplate.id)
      val riskQuestionKey = "RISK_ASSOCIATED_WITH_PLANNED_ACTIVITIES_${UUID.randomUUID()}"
      val adjustmentsQuestionKey = "ADJUSTMENTS_${UUID.randomUUID()}"

      val riskAndAdjustmentsStep = actionPlanStepRepository.save(
        ActionPlanStepFactory()
          .withActionPlanTemplateId(actionPlanTemplate.id)
          .withOrderNumber(11)
          .withName("Risks and adjustments")
          .withStepType(ActionPlanStepType.RISK_AND_ADJUSTMENTS)
          .create(),
      )
      val riskQuestion = actionPlanStepQuestionRepository.save(
        ActionPlanStepQuestionFactory()
          .withActionPlanStepId(riskAndAdjustmentsStep.id)
          .withOrderNumber(1)
          .withTitle("Are there any risks associated with the planned activities?")
          .withQuestionKey(riskQuestionKey)
          .withAnswerType(ActionPlanQuestionAnswerType.RADIO)
          .withMaxNumberResponses(1)
          .create(),
      )
      actionPlanStepQuestionChoiceRepository.save(
        ActionPlanStepQuestionChoiceFactory()
          .withActionPlanStepQuestionId(riskQuestion.id)
          .withOrderNumber(1)
          .withLabel("Yes")
          .withValue("YES")
          .withHasFreeText(true)
          .withFreeTextLabel("Give details about the risks and what you will put in place to reduce them")
          .create(),
      )
      actionPlanStepQuestionChoiceRepository.save(
        ActionPlanStepQuestionChoiceFactory()
          .withActionPlanStepQuestionId(riskQuestion.id)
          .withOrderNumber(2)
          .withLabel("No")
          .withValue("NO")
          .create(),
      )

      val adjustmentsQuestion = actionPlanStepQuestionRepository.save(
        ActionPlanStepQuestionFactory()
          .withActionPlanStepId(riskAndAdjustmentsStep.id)
          .withOrderNumber(2)
          .withTitle("Will you put any reasonable adjustments in place to help Jane take part in the planned activities?")
          .withQuestionKey(adjustmentsQuestionKey)
          .withAnswerType(ActionPlanQuestionAnswerType.RADIO)
          .withMaxNumberResponses(1)
          .create(),
      )
      actionPlanStepQuestionChoiceRepository.save(
        ActionPlanStepQuestionChoiceFactory()
          .withActionPlanStepQuestionId(adjustmentsQuestion.id)
          .withOrderNumber(1)
          .withLabel("Yes")
          .withValue("YES")
          .withHasFreeText(true)
          .withFreeTextLabel("Give details about what reasonable adjustments you will make and how this will support Alice")
          .create(),
      )
      actionPlanStepQuestionChoiceRepository.save(
        ActionPlanStepQuestionChoiceFactory()
          .withActionPlanStepQuestionId(adjustmentsQuestion.id)
          .withOrderNumber(2)
          .withLabel("No")
          .withValue("NO")
          .create(),
      )

      val riskHeader = actionPlanStepQuestionAnswerHeaderRepository.save(
        ActionPlanStepQuestionAnswerHeaderFactory()
          .withId(UUID.randomUUID())
          .withActionPlanId(actionPlan.id)
          .withActionPlanStepQuestionId(riskQuestion.id)
          .withOrderNumber(1)
          .withCreatedAt(OffsetDateTime.now())
          .withCreatedBy(user.hmppsAuthUsername)
          .create(),
      )
      actionPlanStepQuestionAnswerDetailsRepository.save(
        ActionPlanStepQuestionAnswerDetailsFactory()
          .withId(UUID.randomUUID())
          .withActionPlanStepQuestionAnswerHeaderId(riskHeader.id)
          .withRevisionNumber(1)
          .withContent("YES")
          .withFreeTextValue("Potential conflict with another attendee; staff will supervise throughout.")
          .withCreatedAt(OffsetDateTime.now())
          .withCreatedBy(user.hmppsAuthUsername)
          .create(),
      )

      val adjustmentsHeader = actionPlanStepQuestionAnswerHeaderRepository.save(
        ActionPlanStepQuestionAnswerHeaderFactory()
          .withId(UUID.randomUUID())
          .withActionPlanId(actionPlan.id)
          .withActionPlanStepQuestionId(adjustmentsQuestion.id)
          .withOrderNumber(2)
          .withCreatedAt(OffsetDateTime.now())
          .withCreatedBy(user.hmppsAuthUsername)
          .create(),
      )
      actionPlanStepQuestionAnswerDetailsRepository.save(
        ActionPlanStepQuestionAnswerDetailsFactory()
          .withId(UUID.randomUUID())
          .withActionPlanStepQuestionAnswerHeaderId(adjustmentsHeader.id)
          .withRevisionNumber(1)
          .withContent("YES")
          .withFreeTextValue("Provide large-print materials and allow extra time for reading.")
          .withCreatedAt(OffsetDateTime.now())
          .withCreatedBy(user.hmppsAuthUsername)
          .create(),
      )

      val result = actionPlanService.getRiskAndAdjustmentsForReferral(referral.referenceNumber!!)
      assertEquals(2, result.questions.size)

      assertEquals(riskQuestionKey, result.questions[0].key)
      assertEquals(
        listOf("YES"),
        result.questions[0].savedResponses.map { it.value },
      )
      assertEquals(
        listOf("Potential conflict with another attendee; staff will supervise throughout."),
        result.questions[0].savedResponses.map { it.additionalDetails },
      )

      assertEquals(adjustmentsQuestionKey, result.questions[1].key)
      assertEquals(
        listOf("YES"),
        result.questions[1].savedResponses.map { it.value },
      )
      assertEquals(
        listOf("Provide large-print materials and allow extra time for reading."),
        result.questions[1].savedResponses.map { it.additionalDetails },
      )
    }

    private fun createSessionDeliveryStep(actionPlanTemplateId: UUID) = actionPlanStepRepository.save(
      ActionPlanStepFactory()
        .withActionPlanTemplateId(actionPlanTemplateId)
        .withOrderNumber(2)
        .withName("Service Delivery Details")
        .withStepType(ActionPlanStepType.SESSION_DELIVERY)
        .create(),
    )

    private fun createSessionDeliveryQuestion(
      orderNumber: Int,
      title: String,
      answerType: ActionPlanQuestionAnswerType = ActionPlanQuestionAnswerType.RADIO,
      maxNumberResponses: Int = 1,
    ) = actionPlanStepQuestionRepository.save(
      ActionPlanStepQuestionFactory()
        .withActionPlanStepId(sessionDeliveryStep.id)
        .withOrderNumber(orderNumber)
        .withTitle(title)
        .withAnswerType(answerType)
        .withMaxNumberResponses(maxNumberResponses)
        .create(),
    )

    private fun createChoice(
      question: ActionPlanStepQuestion,
      orderNumber: Int,
      label: String,
      value: String,
      hasFreeText: Boolean = false,
      freeTextLabel: String? = null,
    ) = actionPlanStepQuestionChoiceRepository.save(
      ActionPlanStepQuestionChoiceFactory()
        .withActionPlanStepQuestionId(question.id)
        .withOrderNumber(orderNumber)
        .withLabel(label)
        .withValue(value)
        .withHasFreeText(hasFreeText)
        .withFreeTextLabel(freeTextLabel)
        .create(),
    )

    private fun sessionDeliveryDetailsRequest(
      vararg answers: Pair<ActionPlanStepQuestion, List<SessionDeliveryDetailsQuestionAnswer>>,
    ) = ActionPlanSessionDeliveryDetailsRequest(
      answers = answers.map { (question, incomingAnswerDetails) ->
        SessionDeliveryDetailsQuestionAnswers(
          questionId = question.id,
          incomingAnswerDetails = incomingAnswerDetails,
        )
      },
    )

    private fun sessionDeliveryDetailsAnswer(
      value: String,
      additionalDetails: String? = null,
    ) = SessionDeliveryDetailsQuestionAnswer(
      value = value,
      additionalDetails = additionalDetails,
    )

    private fun createReferral(firstName: String, lastName: String): Referral {
      val person = referralHelper.createPerson(
        firstName = firstName,
        lastName = lastName,
        identifier = "X${UUID.randomUUID().toString().take(6).uppercase()}",
      )
      return referralHelper.createReferral(person = person, referenceNumber = randomReferralReference(), submittedBy = user)
    }

    private fun updateSessionDeliveryDetails(request: ActionPlanSessionDeliveryDetailsRequest) = actionPlanService.updateSessionDeliveryDetailsForActionPlan(
      referral.referenceNumber!!,
      request,
      user.id.toString(),
    )
  }
}
