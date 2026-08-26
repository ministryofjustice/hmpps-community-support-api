package uk.gov.justice.digital.hmpps.communitysupportapi.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.ActionPlanSessionDeliveryDetailsRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.SavedResponse
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.SessionDeliveryQuestionRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanQuestionAnswerType
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanQuestionType
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
    val globalTemplate = actionPlanTemplateRepository.getGlobalActionPlanTemplate() ?: throw NotFoundException("Cannot find Global ActionPlan")

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
    val globalTemplate = actionPlanTemplateRepository.getGlobalActionPlanTemplate() ?: throw NotFoundException("Cannot find Global ActionPlan")

    @Test
    fun `should return person details and needs for a referral`() {
      // Given
      val person = referralHelper.createPerson(firstName = "Adam", lastName = "Smith")
      val referral = referralHelper.createReferral(person = person, referenceNumber = randomReferralReference(), submittedBy = user)

      // When
      val result = actionPlanService.getActionPlanSummaryForReferral(referral.referenceNumber!!)

      // Then
      assertEquals("Adam Smith", result.personDetails.fullName)
      assertEquals(needRepository.findAllByOrderByOrderNumberAsc().map { it.label }, result.needs.map { it.label })
      assertTrue(result.needs.all { it.outcomes.isEmpty() })
    }

    @Test
    fun `should return latest revision content for an outcome answer`() {
      // Given
      val person = referralHelper.createPerson(firstName = "Jane", lastName = "Doe")
      val referral = referralHelper.createReferral(person = person, referenceNumber = randomReferralReference(), submittedBy = user)
      val actionPlan = actionPlanHelper.createActionPlan(referralId = referral.id, templateId = globalTemplate.id)
      val need = needRepository.findAllByOrderByOrderNumberAsc().first()
      val outcomeQuestion = findOutcomeQuestionForNeed(globalTemplate.id, need.id)
      val answerId = UUID.randomUUID()

      actionPlanStepQuestionAnswerHeaderRepository.save(
        ActionPlanStepQuestionAnswerHeader(
          id = answerId,
          actionPlanId = actionPlan.id,
          actionPlanStepQuestionId = outcomeQuestion.id,
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
          content = "Initial wording",
          createdAt = OffsetDateTime.now(),
          createdBy = user.id.toString(),
        ),
      )
      actionPlanStepQuestionAnswerDetailsRepository.save(
        ActionPlanStepQuestionAnswerDetails(
          id = UUID.randomUUID(),
          actionPlanStepQuestionAnswerHeaderId = answerId,
          revisionNumber = 2,
          content = "Final wording",
          createdAt = OffsetDateTime.now(),
          createdBy = user.id.toString(),
        ),
      )

      // When
      val result = actionPlanService.getActionPlanSummaryForReferral(referral.referenceNumber!!)

      // Then
      val needSummary = result.needs.first { it.id == need.id }
      assertEquals(listOf("Final wording"), needSummary.outcomes)
    }

    @Test
    fun `should return multiple outcome answers in order for the same need`() {
      // Given
      val person = referralHelper.createPerson(firstName = "Ella", lastName = "Brown")
      val referral = referralHelper.createReferral(person = person, referenceNumber = randomReferralReference(), submittedBy = user)
      val actionPlan = actionPlanHelper.createActionPlan(referralId = referral.id, templateId = globalTemplate.id)
      val need = needRepository.findAllByOrderByOrderNumberAsc().first()
      val outcomeQuestion = findOutcomeQuestionForNeed(globalTemplate.id, need.id)

      val firstAnswerId = UUID.randomUUID()
      val secondAnswerId = UUID.randomUUID()
      actionPlanStepQuestionAnswerHeaderRepository.save(
        ActionPlanStepQuestionAnswerHeader(
          id = firstAnswerId,
          actionPlanId = actionPlan.id,
          actionPlanStepQuestionId = outcomeQuestion.id,
          orderNumber = 1,
          createdAt = OffsetDateTime.now(),
          createdBy = user.id.toString(),
        ),
      )
      actionPlanStepQuestionAnswerHeaderRepository.save(
        ActionPlanStepQuestionAnswerHeader(
          id = secondAnswerId,
          actionPlanId = actionPlan.id,
          actionPlanStepQuestionId = outcomeQuestion.id,
          orderNumber = 2,
          createdAt = OffsetDateTime.now(),
          createdBy = user.id.toString(),
        ),
      )

      actionPlanStepQuestionAnswerDetailsRepository.save(
        ActionPlanStepQuestionAnswerDetails(
          id = UUID.randomUUID(),
          actionPlanStepQuestionAnswerHeaderId = firstAnswerId,
          revisionNumber = 1,
          content = "First outcome",
          createdAt = OffsetDateTime.now(),
          createdBy = user.id.toString(),
        ),
      )
      actionPlanStepQuestionAnswerDetailsRepository.save(
        ActionPlanStepQuestionAnswerDetails(
          id = UUID.randomUUID(),
          actionPlanStepQuestionAnswerHeaderId = secondAnswerId,
          revisionNumber = 1,
          content = "Second outcome",
          createdAt = OffsetDateTime.now(),
          createdBy = user.id.toString(),
        ),
      )

      // When
      val result = actionPlanService.getActionPlanSummaryForReferral(referral.referenceNumber!!)

      // Then
      val needSummary = result.needs.first { it.id == need.id }
      assertEquals(listOf("First outcome", "Second outcome"), needSummary.outcomes)
    }

    @Test
    fun `should ignore soft deleted answers when building outcomes`() {
      // Given
      val person = referralHelper.createPerson(firstName = "Sam", lastName = "Green")
      val referral = referralHelper.createReferral(person = person, referenceNumber = randomReferralReference(), submittedBy = user)
      val actionPlan = actionPlanHelper.createActionPlan(referralId = referral.id, templateId = globalTemplate.id)
      val need = needRepository.findAllByOrderByOrderNumberAsc().first()
      val outcomeQuestion = findOutcomeQuestionForNeed(globalTemplate.id, need.id)

      val activeAnswerId = UUID.randomUUID()
      val deletedAnswerId = UUID.randomUUID()
      actionPlanStepQuestionAnswerHeaderRepository.save(
        ActionPlanStepQuestionAnswerHeader(
          id = activeAnswerId,
          actionPlanId = actionPlan.id,
          actionPlanStepQuestionId = outcomeQuestion.id,
          orderNumber = 1,
          createdAt = OffsetDateTime.now(),
          createdBy = user.id.toString(),
        ),
      )
      actionPlanStepQuestionAnswerHeaderRepository.save(
        ActionPlanStepQuestionAnswerHeader(
          id = deletedAnswerId,
          actionPlanId = actionPlan.id,
          actionPlanStepQuestionId = outcomeQuestion.id,
          orderNumber = 2,
          createdAt = OffsetDateTime.now(),
          createdBy = user.id.toString(),
          deletedAt = OffsetDateTime.now(),
          deletedBy = user.id.toString(),
        ),
      )

      actionPlanStepQuestionAnswerDetailsRepository.save(
        ActionPlanStepQuestionAnswerDetails(
          id = UUID.randomUUID(),
          actionPlanStepQuestionAnswerHeaderId = activeAnswerId,
          revisionNumber = 1,
          content = "Visible outcome",
          createdAt = OffsetDateTime.now(),
          createdBy = user.id.toString(),
        ),
      )
      actionPlanStepQuestionAnswerDetailsRepository.save(
        ActionPlanStepQuestionAnswerDetails(
          id = UUID.randomUUID(),
          actionPlanStepQuestionAnswerHeaderId = deletedAnswerId,
          revisionNumber = 1,
          content = "Hidden outcome",
          createdAt = OffsetDateTime.now(),
          createdBy = user.id.toString(),
        ),
      )

      // When
      val result = actionPlanService.getActionPlanSummaryForReferral(referral.referenceNumber!!)

      // Then
      val needSummary = result.needs.first { it.id == need.id }
      assertEquals(listOf("Visible outcome"), needSummary.outcomes)
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
    val globalTemplate = actionPlanTemplateRepository.getGlobalActionPlanTemplate() ?: throw NotFoundException("Cannot find Global ActionPlan")

    @Test
    fun `should return the latest details for an active answer to the question`() {
      val person = referralHelper.createPerson(firstName = "Chris", lastName = "Taylor")
      val referral = referralHelper.createReferral(person = person, referenceNumber = randomReferralReference(), submittedBy = user)
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
  @DisplayName("getActionPlanNeedsForReferral")
  inner class GetActionPlanNeedsForReferral {
    val user = referralHelper.ensureReferralUser()

    @Test
    fun `should return grouped needs and questions sorted by configured need order`() {
      val (referral, actionPlanTemplateId) = createReferralWithActionPlan("Nina", "Jones")

      val orderedNeeds = needRepository.findAllByOrderByOrderNumberAsc().take(2)
      val firstNeed = orderedNeeds[0]
      val secondNeed = orderedNeeds[1]

      val needStep = createNeedStep(actionPlanTemplateId)
      createNeedQuestion(needStep.id, 1, "Question for second need", secondNeed.id)
      createNeedQuestion(needStep.id, 2, "First question for first need", firstNeed.id)
      createNeedQuestion(needStep.id, 3, "Second question for first need", firstNeed.id)
      createNeedQuestion(needStep.id, 4, "Question without need", null)

      val result = actionPlanService.getActionPlanNeedsForReferral(referral.referenceNumber!!)

      assertEquals(listOf(firstNeed.id, secondNeed.id), result.needs.map { it.id })
      assertEquals(firstNeed.label, result.needs[0].label)
      assertEquals(listOf("First question for first need", "Second question for first need"), result.needs[0].questions.map { it.label })
      assertEquals(
        listOf(ActionPlanQuestionAnswerType.TEXTAREA, ActionPlanQuestionAnswerType.TEXTAREA),
        result.needs[0].questions.map { it.answerType },
      )
      assertEquals(secondNeed.label, result.needs[1].label)
      assertEquals(listOf("Question for second need"), result.needs[1].questions.map { it.label })
      assertEquals(listOf(ActionPlanQuestionAnswerType.TEXTAREA), result.needs[1].questions.map { it.answerType })
    }

    @Test
    fun `should throw not found when referral reference does not exist`() {
      val exception = assertThrows<NotFoundException> {
        actionPlanService.getActionPlanNeedsForReferral("UNKNOWN")
      }

      assertEquals("Referral not found for reference UNKNOWN", exception.message)
    }

    private fun createNeedStep(actionPlanTemplateId: UUID) = actionPlanStepRepository.save(
      ActionPlanStepFactory()
        .withActionPlanTemplateId(actionPlanTemplateId)
        .withOrderNumber(1)
        .withName("Needs")
        .withStepType(ActionPlanStepType.NEED)
        .create(),
    )

    private fun createNeedQuestion(actionPlanStepId: UUID, orderNumber: Int, title: String, needId: UUID?) {
      actionPlanStepQuestionRepository.save(
        ActionPlanStepQuestionFactory()
          .withActionPlanStepId(actionPlanStepId)
          .withOrderNumber(orderNumber)
          .withTitle(title)
          .withAnswerType(ActionPlanQuestionAnswerType.TEXTAREA)
          .withNeedId(needId)
          .create(),
      )
    }

    private fun createReferral(firstName: String, lastName: String): Referral {
      val person = referralHelper.createPerson(firstName = firstName, lastName = lastName)
      return referralHelper.createReferral(person = person, referenceNumber = randomReferralReference(), submittedBy = user)
    }

    private fun createReferralWithActionPlan(firstName: String, lastName: String): Pair<Referral, UUID> {
      val referral = createReferral(firstName, lastName)
      val actionPlanTemplate = actionPlanHelper.createActionPlanTemplate()
      actionPlanHelper.createActionPlan(referralId = referral.id, templateId = actionPlanTemplate.id)
      return referral to actionPlanTemplate.id
    }
  }

  @Nested
  @DisplayName("session delivery details")
  inner class SessionDeliveryDetails {
    val user = referralHelper.ensureReferralUser()

    @Test
    fun `should return saved responses and choices ordered by display order`() {
      val referral = createReferral()
      val actionPlanTemplate = actionPlanHelper.createActionPlanTemplate()
      val actionPlan = actionPlanHelper.createActionPlan(referralId = referral.id, templateId = actionPlanTemplate.id)
      val sessionDeliveryStep = createSessionDeliveryStep(actionPlanTemplate.id)
      val question = createSessionDeliveryQuestion(sessionDeliveryStep.id, 1, "How will the session be delivered?", ActionPlanQuestionAnswerType.RADIO, 1)

      createChoice(question.id, 1, "Face-to-face", "FACE_TO_FACE")
      createChoice(question.id, 2, "Other", "OTHER", hasFreeText = true, freeTextLabel = "Reason for not meeting face-to-face")

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
          content = "OTHER",
          freeTextValue = "Poor weather",
          createdAt = OffsetDateTime.now(),
          createdBy = user.id.toString(),
        ),
      )

      val result = actionPlanService.getSessionDeliveryDetailsForReferral(referral.referenceNumber!!)
      val returnedQuestion = result.questions.single()

      assertEquals(question.id, returnedQuestion.id)
      val choices = returnedQuestion.choices ?: error("Expected choices for session delivery question")
      assertEquals(listOf("FACE_TO_FACE", "OTHER"), choices.map { it.value })
      assertEquals(listOf("Face-to-face", "Other"), choices.map { it.label })
      assertEquals(listOf("OTHER"), returnedQuestion.savedResponses.map { it.value })
      assertEquals(listOf("Poor weather"), returnedQuestion.savedResponses.map { it.additionalDetails })
    }

    @Test
    fun `should save, update, and soft delete session delivery answers`() {
      val referral = createReferral()
      val actionPlanTemplate = actionPlanHelper.createActionPlanTemplate()
      val actionPlan = actionPlanHelper.createActionPlan(referralId = referral.id, templateId = actionPlanTemplate.id)
      val sessionDeliveryStep = createSessionDeliveryStep(actionPlanTemplate.id)

      val radioQuestion = createSessionDeliveryQuestion(sessionDeliveryStep.id, 1, "How will the session be delivered?", ActionPlanQuestionAnswerType.RADIO, 1)
      createChoice(radioQuestion.id, 1, "Face-to-face", "FACE_TO_FACE")
      createChoice(radioQuestion.id, 2, "Other", "OTHER", hasFreeText = true, freeTextLabel = "Reason")

      val checkboxQuestion = createSessionDeliveryQuestion(sessionDeliveryStep.id, 2, "Which communication channels will be used?", ActionPlanQuestionAnswerType.CHECKBOX, 3)
      createChoice(checkboxQuestion.id, 1, "Phone", "PHONE")
      createChoice(checkboxQuestion.id, 2, "Text", "TEXT")

      val saveRequest = ActionPlanSessionDeliveryDetailsRequest(
        answers = listOf(
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

      val saveResult = actionPlanService.updateSessionDeliveryDetailsForActionPlan(referral.referenceNumber!!, saveRequest, user.id.toString())
      assertEquals(listOf("OTHER"), saveResult.questions.first { it.id == radioQuestion.id }.savedResponses.map { it.value })
      assertEquals(listOf("Poor weather"), saveResult.questions.first { it.id == radioQuestion.id }.savedResponses.map { it.additionalDetails })
      assertEquals(listOf("PHONE", "TEXT"), saveResult.questions.first { it.id == checkboxQuestion.id }.savedResponses.map { it.value })

      val updateRequest = ActionPlanSessionDeliveryDetailsRequest(
        answers = listOf(
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

      val updateResult = actionPlanService.updateSessionDeliveryDetailsForActionPlan(referral.referenceNumber!!, updateRequest, user.id.toString())
      assertEquals(listOf("FACE_TO_FACE"), updateResult.questions.first { it.id == radioQuestion.id }.savedResponses.map { it.value })
      assertEquals(listOf(null), updateResult.questions.first { it.id == radioQuestion.id }.savedResponses.map { it.additionalDetails })
      assertEquals(listOf("TEXT"), updateResult.questions.first { it.id == checkboxQuestion.id }.savedResponses.map { it.value })

      val activeAnswers = actionPlanStepQuestionAnswerRepository.findAllByActionPlanIdAndDeletedAtIsNull(actionPlan.id)
      assertEquals(1, activeAnswers.count { it.actionPlanStepQuestionId == radioQuestion.id })
      assertEquals(1, activeAnswers.count { it.actionPlanStepQuestionId == checkboxQuestion.id })
      assertEquals(
        1,
        actionPlanStepQuestionAnswerRepository.findAll().count {
          it.actionPlanId == actionPlan.id &&
            it.actionPlanStepQuestionId == checkboxQuestion.id &&
            it.deletedAt != null
        },
      )

      val radioAnswer = activeAnswers.first { it.actionPlanStepQuestionId == radioQuestion.id }
      val radioRevisions = actionPlanStepQuestionAnswerRevisionRepository
        .findAllByActionPlanStepQuestionAnswerIdIn(listOf(radioAnswer.id))
        .sortedBy { it.revisionNumber }
      assertEquals(listOf("OTHER", "FACE_TO_FACE"), radioRevisions.map { it.content })
      assertEquals(listOf("Poor weather", null), radioRevisions.map { it.freeTextValue })
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
      actionPlanStepId: UUID,
      orderNumber: Int,
      title: String,
      answerType: ActionPlanQuestionAnswerType,
      maxNumberResponses: Int,
    ) = actionPlanStepQuestionRepository.save(
      ActionPlanStepQuestionFactory()
        .withActionPlanStepId(actionPlanStepId)
        .withOrderNumber(orderNumber)
        .withTitle(title)
        .withAnswerType(answerType)
        .withMaxNumberResponses(maxNumberResponses)
        .create(),
    )

    private fun createChoice(
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

    private fun createReferral(): Referral {
      val person = referralHelper.createPerson(firstName = "Jane", lastName = "Doe")
      return referralHelper.createReferral(person = person, referenceNumber = randomReferralReference(), submittedBy = user)
    }
  }
}
