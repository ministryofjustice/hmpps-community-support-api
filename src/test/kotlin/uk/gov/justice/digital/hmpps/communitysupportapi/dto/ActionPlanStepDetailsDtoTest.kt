package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanQuestionAnswerType
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanQuestionType
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanStepQuestion
import java.util.UUID

class ActionPlanStepDetailsDtoTest {
  @Test
  fun `should map ActionPlanStepQuestion entity to DTO`() {
    val question = ActionPlanStepQuestion(
      id = UUID.randomUUID(),
      actionPlanStepId = UUID.randomUUID(),
      orderNumber = 1,
      title = "How are you feeling?",
      answerType = ActionPlanQuestionAnswerType.RADIO,
      questionType = ActionPlanQuestionType.GENERAL,
      maxNumberResponses = 1,
    )

    val dto = ActionPlanStepQuestionDto.fromEntity(question)

    assertEquals(question.id, dto.id)
    assertEquals(question.orderNumber, dto.displayOrder)
    assertEquals(question.title, dto.label)
    assertEquals(question.answerType, dto.answerType)
    assertEquals(question.maxNumberResponses, dto.maximumNumberOfResponses)
  }
}
