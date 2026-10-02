package uk.gov.justice.digital.hmpps.communitysupportapi.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanQuestionAnswerType
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanQuestionType
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanStepType
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.ActionPlanTestSupport
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.ReferralTestSupport
import uk.gov.justice.digital.hmpps.communitysupportapi.model.ActionPlanQuestionAnswers
import java.time.OffsetDateTime
import java.util.UUID

class ActionPlanQuestionWriterIntegrationTest :
  IntegrationTestBase(),
  AfterAllCallback {
  @Autowired
  private lateinit var testSupport: ActionPlanTestSupport

  @Autowired
  private lateinit var referralSupport: ReferralTestSupport

  @Autowired
  private lateinit var writer: ActionPlanQuestionWriter

  @Autowired
  private lateinit var detailsRepository: ActionPlanStepQuestionAnswerDetailsRepository

  @Autowired
  private lateinit var headerRepository: ActionPlanStepQuestionAnswerHeaderRepository

  override fun afterAll(context: ExtensionContext) {
    testDataCleaner.cleanAllTables()
  }

  inner class TestData {
    val actionPlanTemplate = testSupport.createActionPlanTemplate()
    val step = testSupport.createActionPlanStep(actionPlanTemplate.id, stepType = ActionPlanStepType.SESSION_DELIVERY)

    val referral = referralSupport.createReferral()
    val actionPlan = testSupport.createActionPlan(referralId = referral.id, templateId = actionPlanTemplate.id)

    val questionWithAnswer = testSupport.createTextAreaQuestionAndAnswer(step.id, actionPlan.id, "the initial answer", 1)
    val questionWithoutAnswer = testSupport.createActionPlanStepQuestion(
      actionPlanStepId = step.id,
      questionType = ActionPlanQuestionType.GENERAL,
      answerType = ActionPlanQuestionAnswerType.TEXTAREA,
      maxNumberResponses = 1,
      orderNumber = 2,
    )

    val multipleChoiceQuestionWithAnswers = testSupport.createCheckboxQuestionAndAnswers(
      step.id,
      actionPlan.id,
      listOf("Option 1"),
      listOf("Option 1", "Option 2", "Option 3"),
      2,
      3,
    )

    val multipleChoiceQuestionWithMultipleAnswersSelected = testSupport.createCheckboxQuestionAndAnswers(
      step.id,
      actionPlan.id,
      listOf("Option 1", "Option 2"),
      listOf("Option 1", "Option 2", "Option 3"),
      2,
      4,
    )

    val answersForTextareaQuestionWithAnswer = writer.answersForActionPlanAndQuestions(actionPlan.id, listOf(questionWithAnswer.question)).single()
    val answersForMultipleChoiceQuestionWithAnswers = writer.answersForActionPlanAndQuestions(actionPlan.id, listOf(multipleChoiceQuestionWithAnswers.question)).single()
    val answersForMultipleChoiceQuestionWithMultipleAnswersSelected = writer.answersForActionPlanAndQuestions(actionPlan.id, listOf(multipleChoiceQuestionWithMultipleAnswersSelected.question)).single()

    val changedBy = "TEST_USER"
    val changedAt: OffsetDateTime = OffsetDateTime.now()
    val batchId: UUID = UUID.randomUUID()
  }

  @Test
  fun `should persist a changing answer to a textarea question`() {
    // Given
    val testData = TestData()
    val update = ActionPlanQuestionAnswers.Change.Update(
      current = testData.answersForTextareaQuestionWithAnswer.currentAnswers.single(),
      answer = ActionPlanQuestionAnswers.Answer("the updated answer", null),
    )

    // When
    val headerIds = writer.write(testData.answersForTextareaQuestionWithAnswer, listOf(update), testData.changedBy, testData.changedAt, testData.batchId)

    // Then
    val headerId = testData.questionWithAnswer.answerHeader.id
    assertThat(headerIds).containsExactly(headerId)

    val latest = detailsRepository.findAllByActionPlanStepQuestionAnswerHeaderIdIn(listOf(headerId))
      .maxBy { it.revisionNumber }
    assertThat(latest.revisionNumber).isEqualTo(2)
    assertThat(latest.content).isEqualTo("the updated answer")
    assertThat(latest.createdBy).isEqualTo(testData.changedBy)
  }

  @Test
  fun `should persist a checkbox answer moving from 1 to 2 selected options`() {
    // Given
    val testData = TestData()
    val create = ActionPlanQuestionAnswers.Change.Create(
      orderNumber = 2,
      answer = ActionPlanQuestionAnswers.Answer("Option 2", null),
    )

    // When
    val headerIds = writer.write(testData.answersForMultipleChoiceQuestionWithAnswers, listOf(create), testData.changedBy, testData.changedAt, testData.batchId)

    // Then
    val existingHeaderId = testData.multipleChoiceQuestionWithAnswers.answers.single().header.id
    val newHeaderId = headerIds.single()
    assertThat(newHeaderId).isNotEqualTo(existingHeaderId)

    val activeHeaders = headerRepository.findActiveByPlanAndQuestionIds(
      testData.actionPlan.id,
      listOf(testData.multipleChoiceQuestionWithAnswers.question.id),
    )
    assertThat(activeHeaders.map { it.id }).containsExactlyInAnyOrder(existingHeaderId, newHeaderId)

    val detailsByHeader = detailsRepository.findAllByActionPlanStepQuestionAnswerHeaderIdIn(listOf(existingHeaderId, newHeaderId))
      .associateBy { it.actionPlanStepQuestionAnswerHeaderId }
    assertThat(detailsByHeader).hasSize(2)
    assertThat(detailsByHeader.getValue(existingHeaderId).content).isEqualTo("Option 1")
    assertThat(detailsByHeader.getValue(newHeaderId).content).isEqualTo("Option 2")
    assertThat(detailsByHeader.getValue(newHeaderId).revisionNumber).isEqualTo(1)
  }

  @Test
  fun `should persist a checkbox answer moving from 2 to 1 selected options`() {
    // Given
    val testData = TestData()
    val delete = ActionPlanQuestionAnswers.Change.Delete(
      current = testData.answersForMultipleChoiceQuestionWithMultipleAnswersSelected.currentAnswers.single { it.answer.value == "Option 2" },
    )

    // When
    val headerIds = writer.write(testData.answersForMultipleChoiceQuestionWithMultipleAnswersSelected, listOf(delete), testData.changedBy, testData.changedAt, testData.batchId)

    // Then
    val deletedHeaderId = headerIds.single()
    assertThat(deletedHeaderId).isEqualTo(testData.multipleChoiceQuestionWithMultipleAnswersSelected.answers.single { it.details.content == "Option 2" }.header.id)

    val activeHeaders = headerRepository.findActiveByPlanAndQuestionIds(
      testData.actionPlan.id,
      listOf(testData.multipleChoiceQuestionWithMultipleAnswersSelected.question.id),
    )
    assertThat(activeHeaders.map { it.id }).containsExactlyInAnyOrder(
      testData.multipleChoiceQuestionWithMultipleAnswersSelected.answers.single { it.details.content == "Option 1" }.header.id,
    )
  }
}
