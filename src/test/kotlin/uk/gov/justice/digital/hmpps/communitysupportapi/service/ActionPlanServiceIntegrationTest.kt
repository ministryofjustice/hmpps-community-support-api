package uk.gov.justice.digital.hmpps.communitysupportapi.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanQuestionType
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanStepQuestionAnswer
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanStepQuestionAnswerRevision
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanStepType
import uk.gov.justice.digital.hmpps.communitysupportapi.exception.NotFoundException
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.ActionPlanTestSupport
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.ReferralTestSupport
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ActionPlanEventRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ActionPlanRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ActionPlanStepQuestionAnswerRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ActionPlanStepQuestionAnswerRevisionRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ActionPlanStepQuestionRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ActionPlanStepRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ActionPlanTemplateRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.NeedRepository
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
  private lateinit var actionPlanStepQuestionAnswerRepository: ActionPlanStepQuestionAnswerRepository

  @Autowired
  private lateinit var actionPlanStepQuestionAnswerRevisionRepository: ActionPlanStepQuestionAnswerRevisionRepository

  fun afterAll() {
  }

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
      assertEquals(allActionPlans.size, 1)
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
      val referral = referralHelper.createReferral(person = person, referenceNumber = "AB1234CD", submittedBy = user)

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
      val referral = referralHelper.createReferral(person = person, referenceNumber = "ZX1234YZ", submittedBy = user)
      val actionPlan = actionPlanHelper.createActionPlan(referralId = referral.id, templateId = globalTemplate.id)
      val need = needRepository.findAllByOrderByOrderNumberAsc().first()
      val outcomeQuestion = findOutcomeQuestionForNeed(globalTemplate.id, need.id)
      val answerId = UUID.randomUUID()

      actionPlanStepQuestionAnswerRepository.save(
        ActionPlanStepQuestionAnswer(
          id = answerId,
          actionPlanId = actionPlan.id,
          actionPlanStepQuestionId = outcomeQuestion.id,
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
          content = "Initial wording",
          createdAt = OffsetDateTime.now(),
          createdBy = user.id.toString(),
        ),
      )
      actionPlanStepQuestionAnswerRevisionRepository.save(
        ActionPlanStepQuestionAnswerRevision(
          id = UUID.randomUUID(),
          actionPlanStepQuestionAnswerId = answerId,
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
      val referral = referralHelper.createReferral(person = person, referenceNumber = "LK1234MN", submittedBy = user)
      val actionPlan = actionPlanHelper.createActionPlan(referralId = referral.id, templateId = globalTemplate.id)
      val need = needRepository.findAllByOrderByOrderNumberAsc().first()
      val outcomeQuestion = findOutcomeQuestionForNeed(globalTemplate.id, need.id)

      val firstAnswerId = UUID.randomUUID()
      val secondAnswerId = UUID.randomUUID()
      actionPlanStepQuestionAnswerRepository.save(
        ActionPlanStepQuestionAnswer(
          id = firstAnswerId,
          actionPlanId = actionPlan.id,
          actionPlanStepQuestionId = outcomeQuestion.id,
          orderNumber = 1,
          createdAt = OffsetDateTime.now(),
          createdBy = user.id.toString(),
        ),
      )
      actionPlanStepQuestionAnswerRepository.save(
        ActionPlanStepQuestionAnswer(
          id = secondAnswerId,
          actionPlanId = actionPlan.id,
          actionPlanStepQuestionId = outcomeQuestion.id,
          orderNumber = 2,
          createdAt = OffsetDateTime.now(),
          createdBy = user.id.toString(),
        ),
      )

      actionPlanStepQuestionAnswerRevisionRepository.save(
        ActionPlanStepQuestionAnswerRevision(
          id = UUID.randomUUID(),
          actionPlanStepQuestionAnswerId = firstAnswerId,
          revisionNumber = 1,
          content = "First outcome",
          createdAt = OffsetDateTime.now(),
          createdBy = user.id.toString(),
        ),
      )
      actionPlanStepQuestionAnswerRevisionRepository.save(
        ActionPlanStepQuestionAnswerRevision(
          id = UUID.randomUUID(),
          actionPlanStepQuestionAnswerId = secondAnswerId,
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
      val referral = referralHelper.createReferral(person = person, referenceNumber = "GH1234IJ", submittedBy = user)
      val actionPlan = actionPlanHelper.createActionPlan(referralId = referral.id, templateId = globalTemplate.id)
      val need = needRepository.findAllByOrderByOrderNumberAsc().first()
      val outcomeQuestion = findOutcomeQuestionForNeed(globalTemplate.id, need.id)

      val activeAnswerId = UUID.randomUUID()
      val deletedAnswerId = UUID.randomUUID()
      actionPlanStepQuestionAnswerRepository.save(
        ActionPlanStepQuestionAnswer(
          id = activeAnswerId,
          actionPlanId = actionPlan.id,
          actionPlanStepQuestionId = outcomeQuestion.id,
          orderNumber = 1,
          createdAt = OffsetDateTime.now(),
          createdBy = user.id.toString(),
        ),
      )
      actionPlanStepQuestionAnswerRepository.save(
        ActionPlanStepQuestionAnswer(
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

      actionPlanStepQuestionAnswerRevisionRepository.save(
        ActionPlanStepQuestionAnswerRevision(
          id = UUID.randomUUID(),
          actionPlanStepQuestionAnswerId = activeAnswerId,
          revisionNumber = 1,
          content = "Visible outcome",
          createdAt = OffsetDateTime.now(),
          createdBy = user.id.toString(),
        ),
      )
      actionPlanStepQuestionAnswerRevisionRepository.save(
        ActionPlanStepQuestionAnswerRevision(
          id = UUID.randomUUID(),
          actionPlanStepQuestionAnswerId = deletedAnswerId,
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

    private fun findOutcomeQuestionForNeed(templateId: UUID, needId: UUID) = actionPlanStepRepository
      .findAllByActionPlanTemplateIdOrderByOrderNumberAsc(templateId)
      .first { it.needId == needId && it.stepType == ActionPlanStepType.NEED }
      .let { step ->
        actionPlanStepQuestionRepository
          .findAllByActionPlanStepIdInOrderByOrderNumberAsc(listOf(step.id))
          .first { it.questionType == ActionPlanQuestionType.OUTCOME }
      }
  }
}
