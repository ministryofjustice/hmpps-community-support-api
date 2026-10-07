package uk.gov.justice.digital.hmpps.communitysupportapi.repository

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanQuestionResponseEvent
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanQuestionResponseEventType
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanStepQuestionAnswerDetails
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanStepQuestionAnswerHeader
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferenceDataActionPlanStepQuestion
import uk.gov.justice.digital.hmpps.communitysupportapi.model.ActionPlanQuestionAnswers
import java.time.OffsetDateTime
import java.util.UUID

/**
 * A multi-repository utility class for finding current Answers and  recording incoming Answers to ReferenceDataActionPlanStepQuestion.
 * This encapsulates the complex Header -> Details relationship, and the need to record events for each change.
 */
@Component
class ActionPlanQuestionWriter(
  private val headers: ActionPlanStepQuestionAnswerHeaderRepository,
  private val details: ActionPlanStepQuestionAnswerDetailsRepository,
  private val events: ActionPlanQuestionResponseEventRepository,
) {
  fun answersForActionPlanAndQuestions(
    actionPlanId: UUID,
    questions: List<ReferenceDataActionPlanStepQuestion>,
  ): List<ActionPlanQuestionAnswers> {
    if (questions.isEmpty()) return emptyList()
    val activeHeaders = headers.findActiveByPlanAndQuestionIds(actionPlanId, questions.map { it.id })
    val latestDetails = if (activeHeaders.isEmpty()) {
      emptyMap()
    } else {
      details.findAllByActionPlanStepQuestionAnswerHeaderIdIn(activeHeaders.map { it.id })
        .groupBy { it.actionPlanStepQuestionAnswerHeaderId }
        .mapValues { (_, revisions) ->
          revisions.maxWithOrNull(
            compareBy<ActionPlanStepQuestionAnswerDetails> { it.revisionNumber }
              .thenBy { it.createdAt }
              .thenBy { it.id },
          )
        }
    }

    return questions.map { question ->
      ActionPlanQuestionAnswers.from(actionPlanId, question, activeHeaders, latestDetails)
    }
  }

  fun write(
    answers: ActionPlanQuestionAnswers,
    changes: List<ActionPlanQuestionAnswers.Change>,
    changedBy: String,
    changedAt: OffsetDateTime,
    batchId: UUID,
  ): List<UUID> = changes.map { change ->
    val (headerId, eventType) = when (change) {
      is ActionPlanQuestionAnswers.Change.Create -> {
        val header = headers.save(
          ActionPlanStepQuestionAnswerHeader.from(
            answers.actionPlanId,
            answers.question.id,
            change.orderNumber,
            changedBy,
            changedAt,
          ),
        )
        saveDetails(header.id, 1, change.answer, changedBy, changedAt)
        header.id to ActionPlanQuestionResponseEventType.CREATED
      }

      is ActionPlanQuestionAnswers.Change.Update -> {
        saveDetails(change.current.headerId, change.current.revisionNumber + 1, change.answer, changedBy, changedAt)
        change.current.headerId to ActionPlanQuestionResponseEventType.UPDATED
      }

      is ActionPlanQuestionAnswers.Change.Delete -> {
        val header = headers.findById(change.current.headerId).orElseThrow()
        headers.save(header.delete(changedAt, changedBy))
        header.id to ActionPlanQuestionResponseEventType.DELETED
      }
    }

    events.save(
      ActionPlanQuestionResponseEvent.actionPlanQuestionResponseEventForResponses(
        actionPlanId = answers.actionPlanId,
        responseHeaderId = headerId,
        eventType = eventType,
        questionResponseChangeBatchId = batchId,
        createdAt = changedAt,
        createdBy = changedBy,
      ),
    )
    headerId
  }

  private fun saveDetails(
    headerId: UUID,
    revisionNumber: Int,
    answer: ActionPlanQuestionAnswers.Answer,
    changedBy: String,
    changedAt: OffsetDateTime,
  ) {
    details.save(
      ActionPlanStepQuestionAnswerDetails.from(
        headerId,
        revisionNumber,
        answer.value,
        answer.additionalDetails,
        changedBy,
        changedAt,
      ),
    )
  }
}
