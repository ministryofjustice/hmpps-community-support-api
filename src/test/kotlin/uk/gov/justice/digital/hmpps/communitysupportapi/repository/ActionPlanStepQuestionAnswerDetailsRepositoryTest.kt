package uk.gov.justice.digital.hmpps.communitysupportapi.repository

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanStepQuestionAnswerDetails
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanStepQuestionAnswerHeader
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.ActionPlanTestSupport
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.ReferralTestSupport
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.ActionPlanStepFactory
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.ActionPlanStepQuestionFactory
import java.time.OffsetDateTime
import java.util.UUID

class ActionPlanStepQuestionAnswerDetailsRepositoryTest :
  IntegrationTestBase(),
  AfterAllCallback {

  @Autowired
  private lateinit var actionPlanStepQuestionAnswerDetailsRepository: ActionPlanStepQuestionAnswerDetailsRepository

  @Autowired
  private lateinit var actionPlanStepQuestionAnswerHeaderRepository: ActionPlanStepQuestionAnswerHeaderRepository

  @Autowired
  private lateinit var actionPlanStepRepository: ActionPlanStepRepository

  @Autowired
  private lateinit var actionPlanStepQuestionRepository: ActionPlanStepQuestionRepository

  @Autowired
  private lateinit var referralHelper: ReferralTestSupport

  @Autowired
  private lateinit var actionPlanHelper: ActionPlanTestSupport

  override fun afterAll(context: ExtensionContext) {
    testDataCleaner.cleanAllTables()
  }

  @Nested
  @DisplayName("getMostRecentResponsesToQuestionsForActionPlan")
  inner class GetMostRecentResponsesToQuestionsForActionPlan {

    @Test
    fun `should return the single detail when one header has one detail`() {
      // Given
      val user = referralHelper.ensureReferralUser()
      val referral = referralHelper.createReferral(submittedBy = user)
      val template = actionPlanHelper.createActionPlanTemplate()
      val actionPlan = actionPlanHelper.createActionPlan(referralId = referral.id, templateId = template.id)

      val step = actionPlanStepRepository.save(ActionPlanStepFactory().withActionPlanTemplateId(template.id).create())
      val question = actionPlanStepQuestionRepository.save(ActionPlanStepQuestionFactory().withActionPlanStepId(step.id).create())

      val headerId = UUID.randomUUID()
      actionPlanStepQuestionAnswerHeaderRepository.save(
        ActionPlanStepQuestionAnswerHeader(
          id = headerId,
          actionPlanId = actionPlan.id,
          actionPlanStepQuestionId = question.id,
          orderNumber = 1,
          createdAt = OffsetDateTime.now(),
        ),
      )
      val detail = actionPlanStepQuestionAnswerDetailsRepository.save(
        ActionPlanStepQuestionAnswerDetails(
          id = UUID.randomUUID(),
          actionPlanStepQuestionAnswerHeaderId = headerId,
          revisionNumber = 1,
          content = "Only detail",
          createdAt = OffsetDateTime.now(),
        ),
      )

      // When
      val results = actionPlanStepQuestionAnswerDetailsRepository.getMostRecentResponsesToQuestionsForActionPlan(
        questionIds = listOf(question.id),
        actionPlanId = actionPlan.id,
      )

      // Then
      assertEquals(1, results.size)
      assertEquals(detail.id, results.first().id)
      assertEquals("Only detail", results.first().content)
    }

    @Test
    fun `should return only the most recent detail when a header has multiple details`() {
      // Given
      val user = referralHelper.ensureReferralUser()
      val referral = referralHelper.createReferral(submittedBy = user)
      val template = actionPlanHelper.createActionPlanTemplate()
      val actionPlan = actionPlanHelper.createActionPlan(referralId = referral.id, templateId = template.id)

      val step = actionPlanStepRepository.save(ActionPlanStepFactory().withActionPlanTemplateId(template.id).create())
      val question = actionPlanStepQuestionRepository.save(ActionPlanStepQuestionFactory().withActionPlanStepId(step.id).create())

      val headerId = UUID.randomUUID()
      actionPlanStepQuestionAnswerHeaderRepository.save(
        ActionPlanStepQuestionAnswerHeader(
          id = headerId,
          actionPlanId = actionPlan.id,
          actionPlanStepQuestionId = question.id,
          orderNumber = 1,
          createdAt = OffsetDateTime.now(),
        ),
      )

      val olderTime = OffsetDateTime.now().minusMinutes(5)
      val newerTime = OffsetDateTime.now()

      actionPlanStepQuestionAnswerDetailsRepository.save(
        ActionPlanStepQuestionAnswerDetails(
          id = UUID.randomUUID(),
          actionPlanStepQuestionAnswerHeaderId = headerId,
          revisionNumber = 1,
          content = "Older detail",
          createdAt = olderTime,
        ),
      )
      val mostRecentDetail = actionPlanStepQuestionAnswerDetailsRepository.save(
        ActionPlanStepQuestionAnswerDetails(
          id = UUID.randomUUID(),
          actionPlanStepQuestionAnswerHeaderId = headerId,
          revisionNumber = 2,
          content = "Most recent detail",
          createdAt = newerTime,
        ),
      )

      // When
      val results = actionPlanStepQuestionAnswerDetailsRepository.getMostRecentResponsesToQuestionsForActionPlan(
        questionIds = listOf(question.id),
        actionPlanId = actionPlan.id,
      )

      // Then
      assertEquals(1, results.size)
      assertEquals(mostRecentDetail.id, results.first().id)
      assertEquals("Most recent detail", results.first().content)
    }

    @Test
    fun `should return the most recent detail from each header when multiple headers exist for a question`() {
      // Given
      val user = referralHelper.ensureReferralUser()
      val referral = referralHelper.createReferral(submittedBy = user)
      val template = actionPlanHelper.createActionPlanTemplate()
      val actionPlan = actionPlanHelper.createActionPlan(referralId = referral.id, templateId = template.id)

      val step = actionPlanStepRepository.save(ActionPlanStepFactory().withActionPlanTemplateId(template.id).create())
      val question = actionPlanStepQuestionRepository.save(ActionPlanStepQuestionFactory().withActionPlanStepId(step.id).create())

      val firstHeaderId = UUID.randomUUID()
      val secondHeaderId = UUID.randomUUID()

      actionPlanStepQuestionAnswerHeaderRepository.save(
        ActionPlanStepQuestionAnswerHeader(
          id = firstHeaderId,
          actionPlanId = actionPlan.id,
          actionPlanStepQuestionId = question.id,
          orderNumber = 1,
          createdAt = OffsetDateTime.now(),
        ),
      )
      actionPlanStepQuestionAnswerHeaderRepository.save(
        ActionPlanStepQuestionAnswerHeader(
          id = secondHeaderId,
          actionPlanId = actionPlan.id,
          actionPlanStepQuestionId = question.id,
          orderNumber = 2,
          createdAt = OffsetDateTime.now(),
        ),
      )

      val olderTime = OffsetDateTime.now().minusMinutes(5)
      val newerTime = OffsetDateTime.now()

      actionPlanStepQuestionAnswerDetailsRepository.save(
        ActionPlanStepQuestionAnswerDetails(
          id = UUID.randomUUID(),
          actionPlanStepQuestionAnswerHeaderId = firstHeaderId,
          revisionNumber = 1,
          content = "First header - older",
          createdAt = olderTime,
        ),
      )
      val firstHeaderMostRecent = actionPlanStepQuestionAnswerDetailsRepository.save(
        ActionPlanStepQuestionAnswerDetails(
          id = UUID.randomUUID(),
          actionPlanStepQuestionAnswerHeaderId = firstHeaderId,
          revisionNumber = 2,
          content = "First header - most recent",
          createdAt = newerTime,
        ),
      )

      actionPlanStepQuestionAnswerDetailsRepository.save(
        ActionPlanStepQuestionAnswerDetails(
          id = UUID.randomUUID(),
          actionPlanStepQuestionAnswerHeaderId = secondHeaderId,
          revisionNumber = 1,
          content = "Second header - older",
          createdAt = olderTime,
        ),
      )
      val secondHeaderMostRecent = actionPlanStepQuestionAnswerDetailsRepository.save(
        ActionPlanStepQuestionAnswerDetails(
          id = UUID.randomUUID(),
          actionPlanStepQuestionAnswerHeaderId = secondHeaderId,
          revisionNumber = 2,
          content = "Second header - most recent",
          createdAt = newerTime,
        ),
      )

      // When
      val results = actionPlanStepQuestionAnswerDetailsRepository.getMostRecentResponsesToQuestionsForActionPlan(
        questionIds = listOf(question.id),
        actionPlanId = actionPlan.id,
      )

      // Then
      assertEquals(2, results.size)
      val resultIds = results.map { it.id }.toSet()
      assertTrue(firstHeaderMostRecent.id in resultIds)
      assertTrue(secondHeaderMostRecent.id in resultIds)
    }
  }
}
