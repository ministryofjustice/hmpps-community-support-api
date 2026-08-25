package uk.gov.justice.digital.hmpps.communitysupportapi.service

import jakarta.validation.ValidationException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.ActionPlanNeedsResponse
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.ActionPlanSessionDeliveryDetailsRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.ActionPlanSessionDeliveryDetailsResponse
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.ActionPlanStepQuestionRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.ActionPlanSummaryDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.NeedDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.QuestionDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.SessionDeliveryQuestion
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlan
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanEvent
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanQuestionAnswerType
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanQuestionType
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanStepQuestion
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanStepQuestionAnswer
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanStepQuestionAnswerRevision
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
import java.time.OffsetDateTime
import java.util.*

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
    val outcomesByNeedId = actionPlan?.let { getOutcomesByNeedIdForActionPlan(it.id, it.actionPlanTemplateId) }.orEmpty()

    val needs = needRepository.findAllByOrderByOrderNumberAsc().map {
      ActionPlanSummaryDto.ActionPlanSummaryNeed(id = it.id, label = it.label, outcomes = outcomesByNeedId[it.id].orEmpty())
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
        questions.map { SessionDeliveryQuestion.fromQuestionAndResponses(it, emptyList(), it.choices.sortedBy { choice -> choice.orderNumber }) },
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

  //  @Transactional
  //  fun patchSessionDeliveryDetailsForReferral(
  //    referralReference: String,
  //    request: ActionPlanSessionDeliveryDetailsRequest,
  //    changedBy: String,
  //  ): ActionPlanSessionDeliveryDetailsResponse {
  //    val referral = referralRepository.findByReferenceNumber(referralReference).firstOrNull()
  //      ?: throw NotFoundException("Referral not found for reference $referralReference")
  //    val actionPlan = findOrCreateByReferralId(referral.id)
  //
  //    val sessionDeliveryStep = actionPlanStepRepository.findSessionDeliveryStepsByReferralId(referral.id).firstOrNull()
  //      ?: throw NotFoundException("No SESSION_DELIVERY step found for referral $referralReference")
  //
  //    val questions = actionPlanStepQuestionRepository.findAllByActionPlanStepIdOrderByOrderNumberAsc(sessionDeliveryStep.id)
  //    val questionsById = questions.associateBy { it.id }
  //
  //    validateSavedResponseRequest(request, questionsById)
  //
  //    val requestedQuestionIds = request.questions.map { it.id }.toSet()
  //    val existingAnswersByQuestionId = if (requestedQuestionIds.isEmpty()) {
  //      emptyMap()
  //    } else {
  //      actionPlanStepQuestionAnswerRepository
  //        .findAllByActionPlanIdAndActionPlanStepQuestionIdInAndDeletedAtIsNull(actionPlan.id, requestedQuestionIds)
  //        .groupBy { it.actionPlanStepQuestionId }
  //        .mapValues { (_, answers) -> answers.sortedBy { answer -> answer.orderNumber } }
  //    }
  //
  //    val latestRevisionByAnswerId = getLatestRevisionByAnswerId(existingAnswersByQuestionId.values.flatten())
  //    val now = OffsetDateTime.now()
  //
  //    request.questions.forEach { questionRequest ->
  //      val normalisedResponses = questionRequest.savedResponses.map { normaliseSavedResponse(it) }
  //      val existingAnswersForQuestion = existingAnswersByQuestionId[questionRequest.id].orEmpty()
  //      upsertQuestionAnswers(
  //        actionPlanId = actionPlan.id,
  //        questionId = questionRequest.id,
  //        normalisedResponses = normalisedResponses,
  //        existingAnswers = existingAnswersForQuestion,
  //        latestRevisionByAnswerId = latestRevisionByAnswerId,
  //        changedBy = changedBy,
  //        now = now,
  //      )
  //    }
  //
  //    return getSessionDeliveryDetailsForReferral(referralReference)
  //  }

  @Transactional
  fun patchSessionDeliveryDetailsForReferral(
    referralReference: String,
    request: ActionPlanSessionDeliveryDetailsRequest,
    changedBy: String,
  ): ActionPlanSessionDeliveryDetailsResponse {
    val referral = referralRepository.findByReferenceNumber(referralReference).firstOrNull()
      ?: throw NotFoundException("Referral not found for reference $referralReference")

    val actionPlan = findOrCreateByReferralId(referral.id)

    val sessionDeliveryStep = actionPlanStepRepository
      .findSessionDeliveryStepsByReferralId(referral.id)
      .firstOrNull()
      ?: throw NotFoundException("No SESSION_DELIVERY step found for referral $referralReference")

    val questions = actionPlanStepQuestionRepository
      .findAllByActionPlanStepIdOrderByOrderNumberAsc(sessionDeliveryStep.id)
    val questionsById = questions.associateBy { it.id }

    // Ensures every question ID belongs to the Session Delivery step
    validateSavedResponseRequest(request, questionsById)

    patchQuestionAnswers(
      actionPlanId = actionPlan.id,
      actionPlanStepQuestionRequest = request.questions.map { questionRequest ->
        ActionPlanStepQuestionRequest(
          id = questionRequest.id,
          savedResponses = questionRequest.savedResponses,
        )
      },
      changedBy = changedBy,
    )

    return getSessionDeliveryDetailsForReferral(referralReference)
  }

  @Transactional
  fun patchQuestionAnswers(
    actionPlanId: UUID,
    actionPlanStepQuestionRequest: List<ActionPlanStepQuestionRequest>,
    changedBy: String,
  ) {
    val requestedQuestionIds = actionPlanStepQuestionRequest.map { it.id }.toSet()

    val existingAnswersByQuestionId = if (requestedQuestionIds.isEmpty()) {
      emptyMap()
    } else {
      actionPlanStepQuestionAnswerRepository
        .findAllByActionPlanIdAndActionPlanStepQuestionIdInAndDeletedAtIsNull(
          actionPlanId,
          requestedQuestionIds,
        )
        .groupBy { it.actionPlanStepQuestionId }
        .mapValues { (_, answers) -> answers.sortedBy { answer -> answer.orderNumber } }
    }

    val latestRevisionByAnswerId = getLatestRevisionByAnswerId(
      existingAnswersByQuestionId.values.flatten(),
    )
    val now = OffsetDateTime.now()

    actionPlanStepQuestionRequest.forEach { answer ->
      upsertQuestionAnswers(
        actionPlanId = actionPlanId,
        questionId = answer.id,
        normalisedResponses = answer.savedResponses.map { normaliseSavedResponse(it) },
        existingAnswers = existingAnswersByQuestionId[answer.id].orEmpty(),
        latestRevisionByAnswerId = latestRevisionByAnswerId,
        changedBy = changedBy,
        now = now,
      )
    }
  }

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
    val duplicateQuestionIds = request.questions
      .groupingBy { it.id }
      .eachCount()
      .filterValues { count -> count > 1 }
      .keys
    if (duplicateQuestionIds.isNotEmpty()) {
      throw ValidationException("Duplicate question IDs provided: ${duplicateQuestionIds.joinToString(", ")}")
    }

    request.questions.forEach { questionRequest ->
      val question = questionsById[questionRequest.id]
        ?: throw ValidationException("Question ${questionRequest.id} does not belong to session delivery details")

      if (questionRequest.savedResponses.size > question.maxNumberResponses) {
        throw ValidationException(
          "Question ${question.id} accepts at most ${question.maxNumberResponses} responses, got ${questionRequest.savedResponses.size}",
        )
      }

      if ((question.answerType == ActionPlanQuestionAnswerType.RADIO || question.answerType == ActionPlanQuestionAnswerType.TEXTAREA) && questionRequest.savedResponses.size > 1) {
        throw ValidationException("Question ${question.id} is ${question.answerType} and accepts only one response")
      }

      questionRequest.savedResponses.forEach { response ->
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

  private fun upsertQuestionAnswers(
    actionPlanId: UUID,
    questionId: UUID,
    normalisedResponses: List<NormalisedSavedResponse>,
    existingAnswers: List<ActionPlanStepQuestionAnswer>,
    latestRevisionByAnswerId: Map<UUID, ActionPlanStepQuestionAnswerRevision>,
    changedBy: String,
    now: OffsetDateTime,
  ) {
    val retainedAnswersCount = minOf(existingAnswers.size, normalisedResponses.size)

    // Update existing answers
    for (index in 0 until retainedAnswersCount) {
      val existingAnswer = existingAnswers[index]
      val response = normalisedResponses[index]
      val latestRevision = latestRevisionByAnswerId[existingAnswer.id]

      if (latestRevision?.content == response.value && latestRevision.freeTextValue == response.additionalDetails) {
        continue
      }

      actionPlanStepQuestionAnswerRevisionRepository.save(
        ActionPlanStepQuestionAnswerRevision(
          id = UUID.randomUUID(),
          actionPlanStepQuestionAnswerId = existingAnswer.id,
          revisionNumber = (latestRevision?.revisionNumber ?: 0) + 1,
          content = response.value,
          freeTextValue = response.additionalDetails,
          createdAt = now,
          createdBy = changedBy,
        ),
      )
    }

    // Insert new answers
    for (index in retainedAnswersCount until normalisedResponses.size) {
      val response = normalisedResponses[index]
      val answerId = UUID.randomUUID()

      actionPlanStepQuestionAnswerRepository.save(
        ActionPlanStepQuestionAnswer(
          id = answerId,
          actionPlanId = actionPlanId,
          actionPlanStepQuestionId = questionId,
          orderNumber = index + 1,
          createdAt = now,
          createdBy = changedBy,
        ),
      )

      actionPlanStepQuestionAnswerRevisionRepository.save(
        ActionPlanStepQuestionAnswerRevision(
          id = UUID.randomUUID(),
          actionPlanStepQuestionAnswerId = answerId,
          revisionNumber = 1,
          content = response.value,
          freeTextValue = response.additionalDetails,
          createdAt = now,
          createdBy = changedBy,
        ),
      )
    }

    // Soft delete removed answers
    for (index in retainedAnswersCount until existingAnswers.size) {
      val answerToDelete = existingAnswers[index]
      actionPlanStepQuestionAnswerRepository.save(
        answerToDelete.copy(
          deletedAt = now,
          deletedBy = changedBy,
        ),
      )
    }
  }

  private fun getLatestRevisionByAnswerId(answers: List<ActionPlanStepQuestionAnswer>): Map<UUID, ActionPlanStepQuestionAnswerRevision> {
    if (answers.isEmpty()) {
      return emptyMap()
    }

    return actionPlanStepQuestionAnswerRevisionRepository
      .findLatestRevisionsByAnswerIdIn(answers.map { answer -> answer.id })
      .associateBy { revision -> revision.actionPlanStepQuestionAnswerId }
  }

  private fun normaliseSavedResponse(response: SavedResponse): NormalisedSavedResponse = NormalisedSavedResponse(
    value = response.value.trim(),
    additionalDetails = response.additionalDetails?.trim()?.takeIf { details -> details.isNotBlank() },
  )

  private data class NormalisedSavedResponse(
    val value: String,
    val additionalDetails: String?,
  )

  private fun getOutcomesByNeedIdForActionPlan(actionPlanId: UUID, actionPlanTemplateId: UUID): Map<UUID, List<String>> {
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

    val details = actionPlanStepQuestionAnswerDetailsRepository.findAllByActionPlanStepQuestionAnswerHeaderIdIn(answers.map { it.id })
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
