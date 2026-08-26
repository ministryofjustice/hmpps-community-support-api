package uk.gov.justice.digital.hmpps.communitysupportapi.service

import jakarta.validation.ValidationException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.ActionPlanNeedsResponse
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.ActionPlanSessionDeliveryDetailsRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.ActionPlanSessionDeliveryDetailsResponse
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.ActionPlanSummaryDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.NeedDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.QuestionDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.SessionDeliveryQuestion
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlan
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanEvent
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanQuestionAnswerType
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanQuestionType
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanStepQuestion
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanStepQuestionAnswerDetails
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanStepQuestionAnswerHeader
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanStepType
import uk.gov.justice.digital.hmpps.communitysupportapi.exception.NotFoundException
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ActionPlanEventRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ActionPlanRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ActionPlanStepQuestionAnswerDetailsRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ActionPlanStepQuestionAnswerHeaderRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ActionPlanStepQuestionRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ActionPlanStepRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ActionPlanTemplateRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.NeedRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.PersonRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralRepository
import java.util.*

data class AnswerQuestionDetails(
  val actionPlanStepQuestionId: UUID,

  val response: String,
  val createdBy: String,
)

@Service
class ActionPlanService(
  private val actionPlanRepository: ActionPlanRepository,
  private val actionPlanEventRepository: ActionPlanEventRepository,
  private val actionPlanTemplateRepository: ActionPlanTemplateRepository,
  private val actionPlanStepRepository: ActionPlanStepRepository,
  private val actionPlanStepQuestionRepository: ActionPlanStepQuestionRepository,
  private val actionPlanStepQuestionAnswerHeaderRepository: ActionPlanStepQuestionAnswerHeaderRepository,
  private val actionPlanStepQuestionAnswerDetailsRepository: ActionPlanStepQuestionAnswerDetailsRepository,
  private val referralRepository: ReferralRepository,
  private val personRepository: PersonRepository,
  private val needRepository: NeedRepository,
) {
  companion object {
    private val logger = LoggerFactory.getLogger(ActionPlanService::class.java)
  }

  @Transactional
  fun findOrCreateByReferralId(referralId: UUID): ActionPlan = actionPlanRepository.findByReferralId(referralId)
    ?: createForReferral(referralId)

  fun getActionPlanSummaryForReferral(referralReference: String): ActionPlanSummaryDto {
    val referral = referralRepository.findByReferenceNumber(referralReference).firstOrNull()
      ?: throw NotFoundException("Referral not found for reference $referralReference")

    val person = personRepository.findById(referral.personId)
      .orElseThrow { NotFoundException("Person not found for referral $referralReference") }

    val actionPlan = actionPlanRepository.findByReferralId(referral.id)
    val outcomesByNeedId =
      actionPlan?.let { getOutcomesByNeedIdForActionPlan(it.id, it.actionPlanTemplateId) }.orEmpty()

    val needs = needRepository.findAllByOrderByOrderNumberAsc().map {
      ActionPlanSummaryDto.ActionPlanSummaryNeed(
        id = it.id,
        label = it.label,
        outcomes = outcomesByNeedId[it.id].orEmpty(),
      )
    }

    return ActionPlanSummaryDto(
      personDetails = ActionPlanSummaryDto.ActionPlanSummaryPersonDetails(fullName = "${person.firstName} ${person.lastName}"),
      needs = needs,
    )
  }

  fun getActionPlanNeedsForReferral(referralReference: String): ActionPlanNeedsResponse {
    val referral = referralRepository.findByReferenceNumber(referralReference).firstOrNull()
      ?: throw NotFoundException("Referral not found for reference $referralReference")

    val needSteps = actionPlanStepRepository.findNeedStepsByReferralId(referral.id)
    if (needSteps.isEmpty()) {
      logger.warn("No NEED step found for referral {}", referralReference)
      return ActionPlanNeedsResponse(needs = emptyList())
    }

    val questions = actionPlanStepQuestionRepository
      .findAllByActionPlanStepIdOrderByOrderNumberAsc(needSteps.first().id)
      .filter { it.needId != null }

    val needsMap = needRepository.findAllByOrderByOrderNumberAsc().associateBy { it.id }

    val needsList = questions
      .groupBy { it.needId }
      .mapNotNull { (needId, needQuestions) ->
        needId?.let { id ->
          needsMap[id]?.let { need ->
            NeedDto(
              id = need.id,
              label = need.label,
              questions = needQuestions.map { question ->
                QuestionDto(
                  id = question.id,
                  label = question.title,
                  answerType = question.answerType,
                )
              },
            )
          }
        }
      }
      .sortedBy { needsMap[it.id]?.orderNumber ?: Int.MAX_VALUE }

    return ActionPlanNeedsResponse(needs = needsList)
  }

  @Transactional
  fun getSessionDeliveryDetailsForReferral(referralReference: String): ActionPlanSessionDeliveryDetailsResponse {
    val referral = referralRepository.findByReferenceNumber(referralReference).firstOrNull()
      ?: throw NotFoundException("Referral not found for reference $referralReference")

    val actionPlan = findOrCreateByReferralId(referral.id)

    val sessionDeliveryStep = actionPlanStepRepository.findSessionDeliveryStepsByReferralId(referral.id)

    if (sessionDeliveryStep == null) {
      logger.warn("No SESSION_DELIVERY step found for referral {}", referralReference)
      throw NotFoundException("No SESSION_DELIVERY step found for referral $referralReference")
    }

    // Get all questions for the session delivery step, ordered by order number
    val questions = actionPlanStepQuestionRepository
      .findAllByActionPlanStepIdOrderByOrderNumberAsc(sessionDeliveryStep.id)

    val answersToQuestions = actionPlanStepQuestionAnswerHeaderRepository
      .findAllByActionPlanStepQuestionIdIn(questions.map { it.id })
      .sortedBy { answer -> answer.orderNumber }

    if (answersToQuestions.isEmpty()) {
      logger.info("No answers found for session delivery questions for referral {}", referralReference)
      return ActionPlanSessionDeliveryDetailsResponse(
        questions.map {
          SessionDeliveryQuestion.fromQuestionAndResponses(
            it,
            emptyList(),
            it.choices.sortedBy { choice -> choice.orderNumber },
          )
        },
      )
    }

    val answerDetails = actionPlanStepQuestionAnswerDetailsRepository
      .getMostRecentAnswersForActionPlanQuestions(
        questions.map { it.id },
        actionPlan.id,
      )
      .groupBy { it.actionPlanStepQuestionAnswerHeader?.actionPlanStepQuestionId }

    return ActionPlanSessionDeliveryDetailsResponse(
      questions = questions.map { question ->
        val responses = answerDetails[question.id]?.sortedBy { it.actionPlanStepQuestionAnswerHeader?.orderNumber }
        val choices = question.choices.sortedBy { choice -> choice.orderNumber }
        SessionDeliveryQuestion.fromQuestionAndResponses(question, responses ?: emptyList(), choices)
      },
    )
  }

  @Transactional
  fun updateSessionDeliveryDetailsForActionPlan(
    referralReference: String,
    request: ActionPlanSessionDeliveryDetailsRequest,
    changedBy: String,
  ): ActionPlanSessionDeliveryDetailsResponse {
    val referral = referralRepository.findByReferenceNumber(referralReference).firstOrNull()
      ?: throw NotFoundException("Referral not found for reference $referralReference")

    val actionPlan = findOrCreateByReferralId(referral.id)

    val sessionDeliveryStep = actionPlanStepRepository
      .findSessionDeliveryStepsByReferralId(referral.id)
      ?: throw NotFoundException("No SESSION_DELIVERY step found for referral $referralReference")

    val questions = actionPlanStepQuestionRepository
      .findAllByActionPlanStepIdOrderByOrderNumberAsc(sessionDeliveryStep.id)

    val questionsById = questions.associateBy { it.id }

    // Ensures every question ID belongs to the Session Delivery step
    validateSavedResponseRequest(request, questionsById)

    request.answers.forEach { question ->
      question.incomingAnswerDetails.forEach { incomingAnswerDetail ->
        try {
          val headerId = getOrCreateHeaderId(
            actionPlan.id,
            question.questionId,
            incomingAnswerDetail.questionAnswerHeaderId,
            changedBy,
          )

          this.actionPlanStepQuestionAnswerDetailsRepository.save(
            ActionPlanStepQuestionAnswerDetails.from(
              headerId,
              incomingAnswerDetail.value,
              changedBy,
              changedBy,
            ),
          )
        } catch (e: Exception) {
          logger.error(
            "Failed to create StepQuestionAnswerDetails for Question {}, for Referral {}",
            question.questionId,
            referralReference,
          )
        }
      }
    }

    return getSessionDeliveryDetailsForReferral(referralReference)
  }

  private fun getOrCreateHeaderId(
    actionPlanId: UUID,
    questionId: UUID,
    headerId: UUID?,
    createdBy: String,
  ): UUID = headerId ?: this.actionPlanStepQuestionAnswerHeaderRepository.save(
    ActionPlanStepQuestionAnswerHeader.from(actionPlanId, questionId, createdBy),
  ).id

  private fun createForReferral(referralId: UUID): ActionPlan {
    val existingActionPlan = actionPlanRepository.findByReferralId(referralId)
    if (existingActionPlan != null) {
      logger.warn("Action plan already exists for referral {}, skipping creation", referralId)
      return existingActionPlan
    }

    val actionPlanTemplate = actionPlanTemplateRepository.findFirstByActiveGlobalTrueOrderByIdAsc()
      ?: throw NotFoundException("No active global action plan template found")

    val actionPlan = ActionPlan.forReferral(actionPlanTemplate.id, referralId)
    actionPlanRepository.save(actionPlan)

    val actionPlanEvent = ActionPlanEvent.actionPlanCreatedEventForActionPlan(actionPlan.id)
    actionPlanEventRepository.save(actionPlanEvent)

    return actionPlan
  }

  private fun validateSavedResponseRequest(
    request: ActionPlanSessionDeliveryDetailsRequest,
    questionsById: Map<UUID, ActionPlanStepQuestion>,
  ) {
    val duplicateQuestionIds = request.answers
      .groupingBy { it.questionId }
      .eachCount()
      .filterValues { count -> count > 1 }
      .keys

    if (duplicateQuestionIds.isNotEmpty()) {
      throw ValidationException("Duplicate question IDs provided: ${duplicateQuestionIds.joinToString(", ")}")
    }

    request.answers.forEach { questionRequest ->
      val question = questionsById[questionRequest.questionId]
        ?: throw ValidationException("Question ${questionRequest.questionId} does not belong to session delivery details")

      if (questionRequest.incomingAnswerDetails.size > question.maxNumberResponses) {
        throw ValidationException(
          "Question ${question.id} accepts at most ${question.maxNumberResponses} responses, got ${questionRequest.incomingAnswerDetails.size}",
        )
      }

      if ((question.answerType == ActionPlanQuestionAnswerType.RADIO || question.answerType == ActionPlanQuestionAnswerType.TEXTAREA) && questionRequest.incomingAnswerDetails.size > 1) {
        throw ValidationException("Question ${question.id} is ${question.answerType} and accepts only one response")
      }

      questionRequest.incomingAnswerDetails.forEach { response ->
        validateSavedResponseForQuestion(question, response)
      }
    }
  }

  private fun validateSavedResponseForQuestion(question: ActionPlanStepQuestion, response: SavedResponse) {
    val value = response.value.trim()
    if (value.isBlank()) {
      throw ValidationException("Question ${question.id} contains a blank response value")
    }

    when (question.answerType) {
      ActionPlanQuestionAnswerType.TEXTAREA -> {
        if (!response.additionalDetails.isNullOrBlank()) {
          throw ValidationException("Question ${question.id} does not accept additionalDetails")
        }
      }

      ActionPlanQuestionAnswerType.RADIO,
      ActionPlanQuestionAnswerType.CHECKBOX,
      -> {
        val choice = question.choices.firstOrNull { it.value == value }
          ?: throw ValidationException("Question ${question.id} contains unsupported choice value '$value'")

        if (choice.hasFreeText && response.additionalDetails.isNullOrBlank()) {
          throw ValidationException("Question ${question.id} requires additionalDetails for choice '$value'")
        }

        if (!choice.hasFreeText && !response.additionalDetails.isNullOrBlank()) {
          throw ValidationException("Question ${question.id} choice '$value' does not accept additionalDetails")
        }
      }
    }
  }

  private data class NormalisedSavedResponse(
    val value: String,
    val additionalDetails: String?,
  )

  private fun getOutcomesByNeedIdForActionPlan(
    actionPlanId: UUID,
    actionPlanTemplateId: UUID,
  ): Map<UUID, List<String>> {
    val needSteps = actionPlanStepRepository
      .findAllByActionPlanTemplateIdOrderByOrderNumberAsc(actionPlanTemplateId)
      .filter { it.stepType == ActionPlanStepType.NEED }
    if (needSteps.isEmpty()) {
      return emptyMap()
    }

    val questionById = actionPlanStepQuestionRepository
      .findAllByActionPlanStepIdInOrderByOrderNumberAsc(needSteps.map { it.id })
      .filter { it.questionType == ActionPlanQuestionType.OUTCOME && it.needId != null }
      .associateBy { it.id }
    if (questionById.isEmpty()) {
      return emptyMap()
    }

    val answers = actionPlanStepQuestionAnswerHeaderRepository
      .findAllByActionPlanIdAndDeletedAtIsNull(actionPlanId)
      .filter { questionById.containsKey(it.actionPlanStepQuestionId) }
      .sortedBy { it.orderNumber }
    if (answers.isEmpty()) {
      return emptyMap()
    }

    val details =
      actionPlanStepQuestionAnswerDetailsRepository.findAllByActionPlanStepQuestionAnswerHeaderIdIn(answers.map { it.id })
    val latestDetailsByHeaderId = details
      .groupBy { it.actionPlanStepQuestionAnswerHeaderId }
      .mapValues { (_, detailItems) -> detailItems.maxByOrNull { it.revisionNumber } }

    return answers
      .mapNotNull { answer ->
        val question = questionById[answer.actionPlanStepQuestionId] ?: return@mapNotNull null
        val needId = question.needId ?: return@mapNotNull null
        val latestDetails = latestDetailsByHeaderId[answer.id] ?: return@mapNotNull null
        val content = latestDetails.content?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
        needId to content
      }
      .groupBy(keySelector = { it.first }, valueTransform = { it.second })
  }
}
