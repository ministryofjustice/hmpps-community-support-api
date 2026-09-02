package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanQuestionAnswerType
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanStepQuestion
import java.util.UUID

data class SessionDeliveryDetailsQuestionAnswer(
  val value: String,
  val additionalDetails: String? = null,
)

data class SessionDeliveryDetailsQuestionAnswers(
  val questionId: UUID,
  val incomingAnswerDetails: List<SessionDeliveryDetailsQuestionAnswer> = emptyList(),
)

data class ActionPlanStepQuestionDto(
  val id: UUID,
  val displayOrder: Int,
  val label: String,
  val answerType: ActionPlanQuestionAnswerType,
  val maximumNumberOfResponses: Int,
  val choices: List<QuestionChoice>? = null,
  val savedResponses: List<SavedResponse> = emptyList(),
) {
  companion object {
    fun fromEntity(question: ActionPlanStepQuestion): ActionPlanStepQuestionDto = ActionPlanStepQuestionDto(
      id = question.id,
      displayOrder = question.orderNumber,
      label = question.title,
      answerType = question.answerType,
      maximumNumberOfResponses = question.maxNumberResponses,
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
