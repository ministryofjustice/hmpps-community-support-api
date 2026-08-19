package uk.gov.justice.digital.hmpps.communitysupportapi.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanQuestionAnswerType
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanQuestionType
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanStepQuestion
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanStepQuestionChoice
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ActionPlanStepQuestionChoiceRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ActionPlanStepQuestionRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ActionPlanStepRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ActionPlanTemplateRepository
import java.time.OffsetDateTime
import java.util.UUID

@DisplayName("ActionPlanStepQuestionChoice")
class ActionPlanStepQuestionChoiceIntegrationTest :
  IntegrationTestBase(),
  AfterAllCallback {

  @Autowired
  private lateinit var actionPlanStepQuestionChoiceRepository: ActionPlanStepQuestionChoiceRepository

  @Autowired
  private lateinit var actionPlanStepQuestionRepository: ActionPlanStepQuestionRepository

  @Autowired
  private lateinit var actionPlanStepRepository: ActionPlanStepRepository

  @Autowired
  private lateinit var actionPlanTemplateRepository: ActionPlanTemplateRepository

  override fun afterAll(context: ExtensionContext) {
    testDataCleaner.cleanAllTables()
  }

  @Test
  @DisplayName("should save and retrieve a question choice with all fields")
  fun testSaveAndRetrieveQuestionChoice() {
    val globalTemplate = actionPlanTemplateRepository.getGlobalActionPlanTemplate()
      ?: throw IllegalStateException("No global template found")
    val step = actionPlanStepRepository.findAllByActionPlanTemplateIdOrderByOrderNumberAsc(globalTemplate.id).first()

    val question = ActionPlanStepQuestion(
      id = UUID.randomUUID(),
      actionPlanStepId = step.id,
      orderNumber = 100,
      title = "Test Question",
      answerType = ActionPlanQuestionAnswerType.RADIO,
      questionType = ActionPlanQuestionType.GENERAL,
      maxNumberResponses = 1,
    )
    actionPlanStepQuestionRepository.save(question)

    val choiceId = UUID.randomUUID()
    val choice = ActionPlanStepQuestionChoice(
      id = choiceId,
      actionPlanStepQuestionId = question.id,
      orderNumber = 1,
      label = "Option A",
      value = "OPTION_A",
      hasFreeText = false,
      freeTextLabel = null,
      createdAt = OffsetDateTime.now(),
      createdBy = "TEST_USER",
    )

    actionPlanStepQuestionChoiceRepository.save(choice)

    val savedChoice = actionPlanStepQuestionChoiceRepository.findById(choiceId).orElseThrow()
    assertEquals("Option A", savedChoice.label)
    assertEquals("OPTION_A", savedChoice.value)
    assertEquals(1, savedChoice.orderNumber)
    assertEquals(question.id, savedChoice.actionPlanStepQuestionId)
    assertFalse(savedChoice.hasFreeText)
    assertEquals("TEST_USER", savedChoice.createdBy)
  }

  @Test
  @DisplayName("should retrieve choices for a question ordered by order number with free text label")
  fun testRetrieveChoicesOrderedByOrderNumber() {
    val globalTemplate = actionPlanTemplateRepository.getGlobalActionPlanTemplate()
      ?: throw IllegalStateException("No global template found")
    val step = actionPlanStepRepository.findAllByActionPlanTemplateIdOrderByOrderNumberAsc(globalTemplate.id).first()

    val question = ActionPlanStepQuestion(
      id = UUID.randomUUID(),
      actionPlanStepId = step.id,
      orderNumber = 101,
      title = "Multi-choice Question",
      answerType = ActionPlanQuestionAnswerType.RADIO,
      questionType = ActionPlanQuestionType.GENERAL,
      maxNumberResponses = 1,
    )
    actionPlanStepQuestionRepository.save(question)

    val choice1 = ActionPlanStepQuestionChoice(
      id = UUID.randomUUID(),
      actionPlanStepQuestionId = question.id,
      orderNumber = 1,
      label = "First",
      value = "FIRST",
      hasFreeText = false,
      createdAt = OffsetDateTime.now(),
      createdBy = "SYSTEM",
    )
    val choice2 = ActionPlanStepQuestionChoice(
      id = UUID.randomUUID(),
      actionPlanStepQuestionId = question.id,
      orderNumber = 2,
      label = "Second",
      value = "SECOND",
      hasFreeText = false,
      createdAt = OffsetDateTime.now(),
      createdBy = "SYSTEM",
    )
    val choice3 = ActionPlanStepQuestionChoice(
      id = UUID.randomUUID(),
      actionPlanStepQuestionId = question.id,
      orderNumber = 3,
      label = "Third",
      value = "THIRD",
      hasFreeText = true,
      freeTextLabel = "Please specify reason",
      createdAt = OffsetDateTime.now(),
      createdBy = "SYSTEM",
    )

    actionPlanStepQuestionChoiceRepository.saveAll(listOf(choice1, choice2, choice3))

    val retrievedChoices = actionPlanStepQuestionChoiceRepository
      .findByActionPlanStepQuestionIdOrderByOrderNumberAsc(question.id)

    assertEquals(3, retrievedChoices.size)
    assertEquals("FIRST", retrievedChoices[0].value)
    assertEquals("SECOND", retrievedChoices[1].value)
    assertEquals("THIRD", retrievedChoices[2].value)
    assertTrue(retrievedChoices[2].hasFreeText)
    assertEquals("Please specify reason", retrievedChoices[2].freeTextLabel)
  }
}
