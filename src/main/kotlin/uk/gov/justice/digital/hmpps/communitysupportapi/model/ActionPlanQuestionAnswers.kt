package uk.gov.justice.digital.hmpps.communitysupportapi.model

import jakarta.validation.ValidationException
import org.slf4j.LoggerFactory
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanQuestionAnswerType
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanQuestionType
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanStepQuestion
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanStepQuestionAnswerDetails
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanStepQuestionAnswerHeader
import java.util.UUID

/**
 * A container class encapsulating current and incoming answers for ActionPlanStepQuestions
 */
class ActionPlanQuestionAnswers(
  val actionPlanId: UUID,
  val question: ActionPlanStepQuestion,
  val currentAnswers: List<CurrentAnswer>,
) {
  companion object {
    private val logger = LoggerFactory.getLogger(CurrentAnswer::class.java)

    fun from(
      actionPlanId: UUID,
      question: ActionPlanStepQuestion,
      activeHeaders: List<ActionPlanStepQuestionAnswerHeader>,
      latestDetails: Map<UUID, ActionPlanStepQuestionAnswerDetails?>,
    ): ActionPlanQuestionAnswers = ActionPlanQuestionAnswers(
      actionPlanId,
      question,
      activeHeaders.filter { it.actionPlanStepQuestionId == question.id }.map { header ->
        val latest =
          latestDetails[header.id] ?: throw IllegalStateException("No details found for header ${header.id}")
        CurrentAnswer.from(header, latest)
      },
    )
  }

  data class Answer(val value: String, val additionalDetails: String?)

  data class CurrentAnswer(
    val headerId: UUID,
    val orderNumber: Int,
    val revisionNumber: Int,
    val answer: Answer,
  ) {
    companion object {
      fun from(header: ActionPlanStepQuestionAnswerHeader, detail: ActionPlanStepQuestionAnswerDetails): CurrentAnswer = CurrentAnswer(
        header.id,
        header.orderNumber,
        detail.revisionNumber,
        Answer(detail.content ?: "", detail.freeTextValue),
      )
    }
  }

  /**
   * Represents a change to be made to the current answers (against a Header) for a ActionPlanStepQuestion
   */
  sealed interface Change {
    data class Create(val orderNumber: Int, val answer: Answer) : Change
    data class Update(val current: CurrentAnswer, val answer: Answer) : Change
    data class Delete(val current: CurrentAnswer) : Change
  }

  fun changesFor(requested: List<Answer>): List<Change> {
    if (requested.size > question.maxNumberResponses) {
      throw ValidationException("Question ${question.id} accepts at most ${question.maxNumberResponses} responses (${requested.size} provided)")
    }
    requested.forEach(::validate)
    val answers = requested.map { answer ->
      Answer(answer.value.trim(), answer.additionalDetails?.trim()?.takeIf { it.isNotEmpty() })
    }

    if (question.supportsMultipleResponses) {
      val requestedValues = answers.map { it.value }.toSet()
      val changes = currentAnswers.filter { it.answer.value !in requestedValues }.map { Change.Delete(it) }
      var nextOrderNumber = (currentAnswers.maxOfOrNull { it.orderNumber } ?: 0) + 1
      return changes + answers.mapNotNull { answer ->
        val existing = currentAnswers.firstOrNull { it.answer.value == answer.value }
        when {
          existing == null -> Change.Create(nextOrderNumber++, answer)

          existing.answer != answer -> Change.Update(existing, answer)

          else -> {
            logger.debug("No change for question ${question.id} answer '${answer.value}'")
            null
          }
        }
      }
    }

    val existing = currentAnswers.singleOrNull()
    val answer = answers.singleOrNull()
    return when {
      answer == null -> existing?.let { listOf(Change.Delete(it)) }.orEmpty()

      existing == null -> listOf(Change.Create(1, answer))

      existing.answer != answer -> listOf(Change.Update(existing, answer))

      else -> {
        logger.debug("No change for question ${question.id} answer '${answer.value}'")
        emptyList()
      }
    }
  }

  fun replaceFirstOutcome(outcomeId: UUID): Change {
    require(question.questionType == ActionPlanQuestionType.OUTCOME) {
      "replaceFirstOutcome can only be called for questions of type OUTCOME (question ${question.id} is of type ${question.questionType})"
    }
    val answer = Answer(outcomeId.toString(), null)
    return currentAnswers.firstOrNull()?.let { Change.Update(it, answer) } ?: Change.Create(1, answer)
  }

  private fun validate(answer: Answer) {
    val value = answer.value.trim()
    if (value.isBlank()) {
      throw ValidationException("Question ${question.id} contains a blank response value")
    }

    when (question.answerType) {
      ActionPlanQuestionAnswerType.TEXTAREA,
      ActionPlanQuestionAnswerType.DATE,
      -> if (!answer.additionalDetails.isNullOrBlank()) {
        throw ValidationException("Question ${question.id} does not accept additionalDetails")
      }

      ActionPlanQuestionAnswerType.RADIO,
      ActionPlanQuestionAnswerType.CHECKBOX,
      -> {
        val choice = question.choices.firstOrNull { it.value == value }
          ?: throw ValidationException("Question ${question.id} contains unsupported choice value '$value'")
        if (choice.hasFreeText && answer.additionalDetails.isNullOrBlank()) {
          throw ValidationException("Question ${question.id} requires additionalDetails for choice '$value'")
        }
        if (!choice.hasFreeText && !answer.additionalDetails.isNullOrBlank()) {
          throw ValidationException("Question ${question.id} choice '$value' does not accept additionalDetails")
        }
      }
    }
  }
}
