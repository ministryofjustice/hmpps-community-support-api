package uk.gov.justice.digital.hmpps.communitysupportapi.model

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

class ActionPlanQuestionAnswersIntegrationTest :
  IntegrationTestBase(),
  AfterAllCallback {
  @Autowired
  private lateinit var testSupport: ActionPlanTestSupport

  @Autowired
  private lateinit var referralSupport: ReferralTestSupport

  override fun afterAll(context: ExtensionContext) {
    testDataCleaner.cleanAllTables()
  }

  inner class TestData {
    val actionPlanTemplate = testSupport.createActionPlanTemplate()
    val step = testSupport.createActionPlanStep(actionPlanTemplate.id, stepType = ActionPlanStepType.SESSION_DELIVERY)

    val referral = referralSupport.createReferral()
    val actionPlan = testSupport.createActionPlan(referralId = referral.id, templateId = actionPlanTemplate.id)

    val questionWithAnswer = testSupport.createTextAreaQuestionAndAnswer(step.id, actionPlan.id, "the intitial answer", 1)

    val questionWithoutAnswer = testSupport.createActionPlanStepQuestion(
      actionPlanStepId = step.id,
      questionType = ActionPlanQuestionType.GENERAL,
      answerType = ActionPlanQuestionAnswerType.TEXTAREA,
      maxNumberResponses = 1,
      orderNumber = 2,
    )

    val answers = ActionPlanQuestionAnswers.from(
      actionPlanId = actionPlan.id,
      question = questionWithAnswer.question,
      listOf(questionWithAnswer.answerHeader),
      mapOf(questionWithAnswer.answerHeader.id to questionWithAnswer.answerDetails),
    )
  }

  @Test
  fun `should should identify a change in an existing answer`() {
    // Given
    val testData = TestData()
    val incomingAnswer = ActionPlanQuestionAnswers.Answer("the new value", null)

    // When
    val changes = testData.answers.changesFor(listOf(incomingAnswer))

    // Then
    assertThat(changes.size).isEqualTo(1)
    assertThat(changes[0]).isInstanceOf(ActionPlanQuestionAnswers.Change.Update::class.java)
  }

  @Test
  fun `should identify the need to delete an answer`() {
    // Given
    val testData = TestData()

    // When
    val changes = testData.answers.changesFor(emptyList())

    // Then
    assertThat(changes.size).isEqualTo(1)
    assertThat(changes[0]).isInstanceOf(ActionPlanQuestionAnswers.Change.Delete::class.java)
  }

  @Test
  fun `should identify the need to create a new answer`() {
    // Given
    val testData = TestData()
    val newAnswer = ActionPlanQuestionAnswers.Answer("the answer to the previously unanswered question", null)

    // When
    val changes = ActionPlanQuestionAnswers(
      actionPlanId = testData.actionPlan.id,
      question = testData.questionWithoutAnswer,
      currentAnswers = emptyList(),
    ).changesFor(listOf(newAnswer))

    // Then
    assertThat(changes.size).isEqualTo(1)
    assertThat(changes[0]).isInstanceOf(ActionPlanQuestionAnswers.Change.Create::class.java)
  }
}
