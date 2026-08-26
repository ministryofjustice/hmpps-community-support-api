package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanQuestionAnswerType
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanStepQuestion
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanStepQuestionAnswerDetails
import java.util.UUID
import kotlin.collections.map

data class ActionPlanSessionDeliveryDetailsResponse(
  val questions: List<SessionDeliveryQuestion>,
)

class SessionDeliveryQuestion(
  val id: UUID,
  val displayOrder: Int,
  val label: String,
  val answerType: ActionPlanQuestionAnswerType,
  val maximumNumberOfResponses: Int,
  val choices: List<QuestionChoice>? = null,
  val savedResponses: List<SavedResponse> = emptyList(),
) {
  companion object {
    fun fromQuestionAndResponses(
      question: ActionPlanStepQuestion,
      responses: List<ActionPlanStepQuestionAnswerDetails>,
    ): SessionDeliveryQuestion = SessionDeliveryQuestion(
      displayOrder = question.orderNumber,
      id = question.id,
      label = question.title,
      answerType = question.answerType,
      maximumNumberOfResponses = question.maxNumberResponses,
      savedResponses = responses.map { response ->
        SavedResponse(response.content ?: "", response.freeTextValue)
      },
      choices = question.choices
        .sortedBy { it.orderNumber }
        .map { choice ->
          QuestionChoice(
            value = choice.value,
            label = choice.label,
            displayAdditionalDetailsOnSelect = choice.hasFreeText,
            additionalDetailsLabel = if (choice.hasFreeText) choice.freeTextLabel else null,
            displayOrder = choice.orderNumber,
          )
        }
        .takeIf { it.isNotEmpty() },
    )
  }
}

data class QuestionChoice(
  val value: String,
  val label: String,
  val displayOrder: Int,
  val displayAdditionalDetailsOnSelect: Boolean = false,
  val additionalDetailsLabel: String?,
)

data class SavedResponse(
  val value: String,
  val additionalDetails: String? = null,
)
