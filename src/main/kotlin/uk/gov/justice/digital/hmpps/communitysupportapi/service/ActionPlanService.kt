package uk.gov.justice.digital.hmpps.communitysupportapi.service

import jakarta.validation.ValidationException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.communitysupportapi.datafetcher.ActionPlanDataFetcher
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.ActionPlanActionRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.ActionPlanActionResponse
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.ActionPlanNeedsOrderRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.ActionPlanSelectANeedNeed
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.ActionPlanSelectANeedOutcome
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.ActionPlanSelectANeedResponse
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.ActionPlanSessionDeliveryDetailsRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.ActionPlanSessionDeliveryDetailsResponse
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.ActionPlanStepQuestionDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.ActionPlanSummaryDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.QuestionChoice
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.SessionDeliveryDetailsQuestionAnswers
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.SessionDeliveryQuestion
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlan
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanActivity
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanQuestionType
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanStepQuestionAnswerDetails
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanStepQuestionAnswerHeader
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanStepType
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferenceDataActionPlanStep
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferenceDataActionPlanStepQuestion
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Referral
import uk.gov.justice.digital.hmpps.communitysupportapi.exception.NotFoundException
import uk.gov.justice.digital.hmpps.communitysupportapi.model.ActionPlanQuestionAnswers
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ActionPlanActivityRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ActionPlanNeedsAndOutcomesViewRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ActionPlanQuestionWriter
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ActionPlanStepQuestionAnswerDetailsRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ActionPlanStepQuestionAnswerHeaderRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ActionPlanStepQuestionRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.NeedRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.OutcomeRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.PersonRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.service.placeholder.PlaceholderFactory
import uk.gov.justice.digital.hmpps.communitysupportapi.util.PlaceholderUtils
import java.time.OffsetDateTime
import java.util.UUID

@Service
class ActionPlanService(
  private val actionPlanStepQuestionRepository: ActionPlanStepQuestionRepository,
  private val actionPlanStepQuestionAnswerHeaderRepository: ActionPlanStepQuestionAnswerHeaderRepository,
  private val actionPlanStepQuestionAnswerDetailsRepository: ActionPlanStepQuestionAnswerDetailsRepository,
  private val actionPlanActivityRepository: ActionPlanActivityRepository,
  private val personRepository: PersonRepository,
  private val needRepository: NeedRepository,
  private val outcomeRepository: OutcomeRepository,
  private val actionPlanDataFetcher: ActionPlanDataFetcher,
  private val placeholderFactory: PlaceholderFactory,
  private val actionPlanQuestionWriter: ActionPlanQuestionWriter,
  private val actionPlanNeedsAndOutcomesViewRepository: ActionPlanNeedsAndOutcomesViewRepository,
) {
  companion object {
    private val logger = LoggerFactory.getLogger(ActionPlanService::class.java)
  }

  fun getActionPlanSummaryForReferral(referralReference: String): ActionPlanSummaryDto {
    val actionPlanData = actionPlanDataFetcher.getActionPlanDataForReferral(referralReference)
    val actionPlan = actionPlanData.actionPlan

    val person = personRepository.findById(actionPlanData.referral.personId)
      .orElseThrow { NotFoundException("Person not found for referral $referralReference") }

    val outcomes = actionPlanNeedsAndOutcomesViewRepository.findByIdReferralIdOrderByDisplayOrderAsc(actionPlanData.referral.id)

    val activitiesByHeaderId = actionPlanActivityRepository.findAllByActionPlanStepQuestionAnswerHeaderIdIn(outcomes.map { it.id.actionPlanStepQuestionAnswerHeaderId })
      .groupBy { it.actionPlanStepQuestionAnswerHeaderId }

    val needs = outcomes.map { outcome ->
      val content = ActionPlanSummaryDto.ActionPlanSummaryOutcome.from(
        outcome.id.actionPlanStepQuestionAnswerHeaderId,
        outcome.outcomeAnswerText,
        activitiesByHeaderId[outcome.id.actionPlanStepQuestionAnswerHeaderId],
      )
      ActionPlanSummaryDto.ActionPlanSummaryNeed(
        id = outcome.needId,
        label = outcome.needName,
        orderNumber = outcome.displayOrder,
        outcomes = listOf(content),
      )
    }

    return ActionPlanSummaryDto(
      personDetails = ActionPlanSummaryDto.ActionPlanSummaryPersonDetails(person.firstName, person.lastName),
      needs = needs,
      lastUpdatedAt = actionPlan.updatedAt,
    )
  }

  @Transactional
  fun updateNeedsOrder(referralReference: String, request: ActionPlanNeedsOrderRequest) {
    fun swapOrderOfNeeds(firstNeed: ActionPlanStepQuestionAnswerHeader, secondNeed: ActionPlanStepQuestionAnswerHeader) {
      val firstNeedOrderNumber = firstNeed.orderNumber
      actionPlanStepQuestionAnswerHeaderRepository.save(firstNeed.updateOrderNumber(secondNeed.orderNumber))
      actionPlanStepQuestionAnswerHeaderRepository.save(secondNeed.updateOrderNumber(firstNeedOrderNumber))
    }

    val actionPlanData = actionPlanDataFetcher.getActionPlanDataForReferral(referralReference)

    val answers = getAnswerHeadersByActionPlan(actionPlanData.actionPlan.id, actionPlanData.needSteps)
    if (answers.none { it.id == request.stepQuestionAnswerHeaderId }) {
      throw Exception("needId (${request.stepQuestionAnswerHeaderId}) in request does not match any needs in the action plan")
    }
    val requestAnswer = answers.find { it.id == request.stepQuestionAnswerHeaderId }

    when (request.action) {
      ActionPlanNeedsOrderRequest.ActionPlanNeedsOrderAction.UP -> {
        // Only swap if requested need not already at the top of the list
        if (requestAnswer != null && requestAnswer.orderNumber > 1) {
          swapOrderOfNeeds(requestAnswer, answers[(requestAnswer.orderNumber - 1) - 1])
        }
      }

      ActionPlanNeedsOrderRequest.ActionPlanNeedsOrderAction.DOWN -> {
        // Only swap if requested need not already at the bottom of the list
        if (requestAnswer != null && requestAnswer.orderNumber <= answers.size - 1) {
          swapOrderOfNeeds(requestAnswer, answers[(requestAnswer.orderNumber - 1) + 1])
        }
      }
    }
  }

  @Transactional(readOnly = true)
  fun getNeedsAndOutcomesForActionPlan(): ActionPlanSelectANeedResponse = ActionPlanSelectANeedResponse(
    needs = needRepository.findAllByOrderByOrderNumberAsc().map { need ->
      ActionPlanSelectANeedNeed(
        id = need.id,
        label = need.label,
        outcomes = need.referenceDataOutcomes.map { outcome ->
          ActionPlanSelectANeedOutcome(
            id = outcome.id,
            text = outcome.text,
          )
        },
      )
    },
  )

  @Transactional(readOnly = true)
  fun getSessionDeliveryDetailsForReferral(referralReference: String): ActionPlanSessionDeliveryDetailsResponse {
    val actionPlanData = actionPlanDataFetcher.getSessionDeliveryDataForReferral(referralReference)
    return buildQuestionResponse(
      actionPlanId = actionPlanData.actionPlan.id,
      referral = actionPlanData.referral,
      steps = listOf(actionPlanData.step),
    )
  }

  @Transactional(readOnly = true)
  fun getRiskAndAdjustmentsForReferral(referralReference: String): ActionPlanSessionDeliveryDetailsResponse {
    val data = actionPlanDataFetcher.getRiskAndAdjustmentsDataForReferral(referralReference)
    return buildQuestionResponse(
      actionPlanId = data.actionPlan.id,
      referral = data.referral,
      steps = listOf(data.step),
    )
  }

  @Transactional(readOnly = true)
  fun getConfirmServiceEndDateForReferral(referralReference: String): ActionPlanSessionDeliveryDetailsResponse {
    val data = actionPlanDataFetcher.getConfirmServiceEndDateForReferral(referralReference)
    return buildQuestionResponse(
      actionPlanId = data.actionPlan.id,
      referral = data.referral,
      steps = listOf(data.step),
    )
  }

  @Transactional(readOnly = true)
  fun getUpdateServiceEndDateForReferral(referralReference: String): ActionPlanSessionDeliveryDetailsResponse {
    val data = actionPlanDataFetcher.getUpdateServiceEndDateForReferral(referralReference)
    return buildQuestionResponse(
      actionPlanId = data.actionPlan.id,
      referral = data.referral,
      steps = listOf(data.step),
    )
  }

  @Transactional(readOnly = true)
  fun getSessionDeliveryCheckYourAnswersForReferral(
    referralReference: String,
    isServiceEndDateChanged: Boolean,
  ): ActionPlanSessionDeliveryDetailsResponse {
    val data = actionPlanDataFetcher.getCheckYourAnswersForReferral(referralReference, isServiceEndDateChanged)
    return buildQuestionResponse(
      actionPlanId = data.actionPlan.id,
      referral = data.referral,
      steps = data.steps,
    )
  }

  @Transactional(readOnly = true)
  fun getPersonInvolvementForReferral(referralReference: String): ActionPlanSessionDeliveryDetailsResponse {
    val data = actionPlanDataFetcher.getPersonInvolvementForReferral(referralReference)
    return buildQuestionResponse(
      actionPlanId = data.actionPlan.id,
      referral = data.referral,
      steps = listOf(data.step),
    )
  }

  private fun buildQuestionResponse(
    actionPlanId: UUID,
    referral: Referral,
    steps: List<ReferenceDataActionPlanStep>,
  ): ActionPlanSessionDeliveryDetailsResponse {
    val questions = steps.flatMap { step ->
      actionPlanStepQuestionRepository.findAllByActionPlanStepIdOrderByOrderNumberAsc(step.id)
    }

    val activeHeaders = actionPlanStepQuestionAnswerHeaderRepository
      .findActiveByPlanAndQuestionIds(actionPlanId, questions.map { it.id })

    val answerDetails = actionPlanStepQuestionAnswerDetailsRepository
      .findAllByActionPlanStepQuestionAnswerHeaderIdIn(activeHeaders.map { it.id })

    val response = ActionPlanSessionDeliveryDetailsResponse(
      questions = questions.map { question ->
        val questionDto = ActionPlanStepQuestionDto.fromEntity(question)
        val responses = activeHeaders
          .filter { it.actionPlanStepQuestionId == question.id }
          .mapNotNull { header ->
            answerDetails
              .filter { it.actionPlanStepQuestionAnswerHeaderId == header.id }
              .maxWithOrNull(
                compareBy<ActionPlanStepQuestionAnswerDetails> { it.revisionNumber }
                  .thenBy { it.createdAt }
                  .thenBy { it.id },
              )
          }
        val choices = question.choices.sortedBy { choice -> choice.orderNumber }
        SessionDeliveryQuestion.fromQuestionAndResponses(questionDto, responses, choices)
      },
    )

    return renderQuestionPlaceholders(response, referral)
  }

  private fun renderQuestionPlaceholders(
    response: ActionPlanSessionDeliveryDetailsResponse,
    referral: Referral,
  ): ActionPlanSessionDeliveryDetailsResponse {
    val tokens = PlaceholderUtils.extractTokens(collectQuestionTemplates(response))
    if (tokens.isEmpty()) return response

    val placeholders = placeholderFactory.forReferral(tokens, referral)
    if (placeholders.isEmpty()) return response

    return ActionPlanSessionDeliveryDetailsResponse(
      questions = response.questions.map { question ->
        SessionDeliveryQuestion(
          id = question.id,
          displayOrder = question.displayOrder,
          label = PlaceholderUtils.render(question.label, *placeholders) ?: question.label,
          key = question.key,
          hint = PlaceholderUtils.render(question.hint, *placeholders),
          answerType = question.answerType,
          maximumNumberOfResponses = question.maximumNumberOfResponses,
          choices = question.choices?.map { choice ->
            QuestionChoice(
              value = choice.value,
              label = PlaceholderUtils.render(choice.label, *placeholders) ?: choice.label,
              displayOrder = choice.displayOrder,
              displayAdditionalDetailsOnSelect = choice.displayAdditionalDetailsOnSelect,
              additionalDetailsLabel = PlaceholderUtils.render(choice.additionalDetailsLabel, *placeholders),
              additionalDetailsHint = PlaceholderUtils.render(choice.additionalDetailsHint, *placeholders),
            )
          },
          savedResponses = question.savedResponses,
        )
      },
    )
  }

  private fun collectQuestionTemplates(
    response: ActionPlanSessionDeliveryDetailsResponse,
  ): List<String?> = response.questions.flatMap { question ->
    listOf(question.label, question.hint) +
      question.choices.orEmpty().flatMap { choice ->
        listOf(choice.label, choice.additionalDetailsLabel, choice.additionalDetailsHint)
      }
  }

  @Transactional
  fun updateSessionDeliveryDetailsForActionPlan(
    referralReference: String,
    request: ActionPlanSessionDeliveryDetailsRequest,
    changedBy: String,
    changedAt: OffsetDateTime = OffsetDateTime.now(),
  ): ActionPlanSessionDeliveryDetailsResponse {
    val actionPlanData = actionPlanDataFetcher.getServiceDeliveryDetailsDataForReferral(referralReference)
    val actionPlan = actionPlanData.actionPlan
    val baseSteps = actionPlanData.steps
      .filter { it.stepType != ActionPlanStepType.CHANGE_SERVICE_END_DATE }
    val confirmServiceEndDateStep = baseSteps
      .single { it.stepType == ActionPlanStepType.SERVICE_END_DATE_CHECK }
    val baseQuestions = actionPlanStepQuestionRepository
      .findAllByActionPlanStepIdInOrderByOrderNumberAsc(baseSteps.map { it.id })
    val baseExistingHeaders = actionPlanStepQuestionAnswerHeaderRepository
      .findActiveByPlanAndQuestionIds(
        actionPlan.id,
        baseQuestions.map { it.id },
      )
    val baseExistingAnswerDetails = if (baseExistingHeaders.isEmpty()) {
      emptyList()
    } else {
      actionPlanStepQuestionAnswerDetailsRepository
        .findAllByActionPlanStepQuestionAnswerHeaderIdIn(baseExistingHeaders.map { it.id })
    }
    val shouldHandleChangeServiceEndDateStep = isChangeServiceEndDateRequired(
      confirmServiceEndDateStep.id,
      baseQuestions,
      request.answers,
      baseExistingHeaders,
      baseExistingAnswerDetails,
    )
    val changeServiceEndDateStep = if (shouldHandleChangeServiceEndDateStep) {
      actionPlanData.steps.firstOrNull { it.stepType == ActionPlanStepType.CHANGE_SERVICE_END_DATE }
        ?: actionPlanDataFetcher.getUpdateServiceEndDateForReferral(referralReference).step
    } else {
      null
    }
    val steps = baseSteps + listOfNotNull(changeServiceEndDateStep)
    val questions = actionPlanStepQuestionRepository
      .findAllByActionPlanStepIdInOrderByOrderNumberAsc(steps.map { it.id })

    request.answers.forEach { answer ->
      val question = questions.find { it.id == answer.questionId }
        ?: throw ValidationException("Question ${answer.questionId} does not belong to session delivery details")

      if (answer.incomingAnswerDetails.size > question.maxNumberResponses) {
        throw ValidationException("Question ${question.id} accepts at most ${question.maxNumberResponses} responses")
      }
    }

    val answersByQuestion = actionPlanQuestionWriter.answersForActionPlanAndQuestions(actionPlan.id, questions).associateBy { it.question.id }
    val changes = request.answers.groupBy { it.questionId }.map { (questionId, submitted) ->
      val answers = answersByQuestion.getValue(questionId)
      val requested = submitted
        .flatMap { it.incomingAnswerDetails }
        .map { ActionPlanQuestionAnswers.Answer(it.value, it.additionalDetails) }
      answers to answers.changesFor(requested)
    }
    val batchId = UUID.randomUUID()
    changes.forEach { (answers, decisions) ->
      actionPlanQuestionWriter.write(answers, decisions, changedBy, changedAt, batchId)
    }

    return getSessionDeliveryDetailsForReferral(referralReference)
  }

  private fun isChangeServiceEndDateRequired(
    confirmServiceEndDateStepId: UUID,
    questions: List<ReferenceDataActionPlanStepQuestion>,
    incomingAnswers: List<SessionDeliveryDetailsQuestionAnswers>,
    existingHeaders: List<ActionPlanStepQuestionAnswerHeader>,
    existingAnswerDetails: List<ActionPlanStepQuestionAnswerDetails>,
  ): Boolean {
    val confirmQuestionId = questions
      .singleOrNull { it.actionPlanStepId == confirmServiceEndDateStepId }
      ?.id
      ?: return false

    val confirmAnswer = incomingAnswers
      .firstOrNull { it.questionId == confirmQuestionId }
      ?.incomingAnswerDetails
      ?.singleOrNull()
      ?.value
      ?.trim()
      ?: getLatestAnswerContent(confirmQuestionId, existingHeaders, existingAnswerDetails)

    return confirmAnswer.equals("NO", ignoreCase = true)
  }

  private fun getLatestAnswerContent(
    questionId: UUID,
    existingHeaders: List<ActionPlanStepQuestionAnswerHeader>,
    existingAnswerDetails: List<ActionPlanStepQuestionAnswerDetails>,
  ): String? = existingHeaders
    .firstOrNull { it.actionPlanStepQuestionId == questionId }
    ?.let { header ->
      existingAnswerDetails
        .filter { it.actionPlanStepQuestionAnswerHeaderId == header.id }
        .maxWithOrNull(
          compareBy<ActionPlanStepQuestionAnswerDetails> { it.revisionNumber }
            .thenBy { it.createdAt }
            .thenBy { it.id },
        )
        ?.content
        ?.trim()
    }

  private fun getQuestionsMapByQuestionId(needSteps: List<ReferenceDataActionPlanStep>): Map<UUID, ReferenceDataActionPlanStepQuestion> = actionPlanStepQuestionRepository
    .findAllByActionPlanStepIdInOrderByOrderNumberAsc(needSteps.map { it.id })
    .filter { it.questionType == ActionPlanQuestionType.OUTCOME && it.needId != null }
    .associateBy { it.id }

  private fun getAnswerHeadersByActionPlan(actionPlanId: UUID, needSteps: List<ReferenceDataActionPlanStep>): List<ActionPlanStepQuestionAnswerHeader> {
    val questionById = getQuestionsMapByQuestionId(needSteps)

    val answers = actionPlanStepQuestionAnswerHeaderRepository
      .findAllByActionPlanIdAndDeletedAtIsNull(actionPlanId)
      .filter { questionById.containsKey(it.actionPlanStepQuestionId) }
      .sortedBy { it.orderNumber }
    return answers
  }

  @Transactional
  fun submitActionForReferral(
    referralReference: String,
    request: ActionPlanActionRequest,
    changedBy: String,
  ): ActionPlanActionResponse {
    val changedAt = OffsetDateTime.now()
    val actionPlanData = actionPlanDataFetcher.getActionPlanDataForReferral(referralReference)
    val actionPlan = actionPlanData.actionPlan
    val needSteps = actionPlanData.needSteps

    // find the outcome for our action plan step
    val outcome = outcomeRepository.findById(request.outcomeId)
      .orElseThrow { NotFoundException("Outcome not found with id=${request.outcomeId}") }

    // check the outcome belongs to the need
    if (outcome.needId != request.needId) {
      throw ValidationException("Outcome ${request.outcomeId} does not belong to need ${request.needId}")
    }

    // find the need step question for our action plan
    val needStepQuestions = actionPlanStepQuestionRepository
      .findAllByActionPlanStepIdInOrderByOrderNumberAsc(needSteps.map { it.id })
      .filter { it.questionType == ActionPlanQuestionType.OUTCOME && it.needId == request.needId }

    if (needStepQuestions.isEmpty()) {
      throw NotFoundException("No outcome question found for need ${request.needId}")
    }

    val question = needStepQuestions.first()
    val questionResponseChangeBatchId = UUID.randomUUID()
    val answers = actionPlanQuestionWriter.answersForActionPlanAndQuestions(actionPlan.id, listOf(question)).single()
    val numberOfActiveAnswers = actionPlanQuestionWriter.answersForActionPlan(actionPlan.id).size
    val change = answers.replaceFirstOutcome(request.outcomeId, numberOfActiveAnswers + 1)
    if (change is ActionPlanQuestionAnswers.Change.Update) {
      actionPlanActivityRepository.deleteByActionPlanStepQuestionAnswerHeaderId(change.current.headerId)
    }
    val answerHeaderId = actionPlanQuestionWriter.write(answers, listOf(change), changedBy, changedAt, questionResponseChangeBatchId).single()

    request.activities.forEach { activity ->
      actionPlanActivityRepository.save(
        ActionPlanActivity(
          id = UUID.randomUUID(),
          actionPlanStepQuestionAnswerHeaderId = answerHeaderId,
          who = activity.who,
          activityDetails = activity.activityDetails,
          status = activity.status,
        ),
      )
    }

    logger.info(
      "Successfully submitted action for referral={} with need={} and outcome={}",
      referralReference,
      request.needId,
      request.outcomeId,
    )

    return ActionPlanActionResponse(
      success = true,
      message = "Action submitted successfully",
    )
  }

  @Transactional
  fun deleteActionForReferral(
    referralReference: String,
    actionId: UUID,
    changedBy: String,
  ): ActionPlanActionResponse {
    val changedAt = OffsetDateTime.now()
    val actionPlanData = actionPlanDataFetcher.getActionPlanDataForReferral(referralReference)
    val actionPlan = actionPlanData.actionPlan
    val activeAnswers = actionPlanQuestionWriter.answersForActionPlan(actionPlan.id)

    val targetAction = activeAnswers
      .firstOrNull { it.id == actionId }
      ?: throw NotFoundException("Action not found with id=$actionId")

    val question = actionPlanStepQuestionRepository.findById(targetAction.actionPlanStepQuestionId)
      .orElseThrow { NotFoundException("Question not found for action $actionId") }

    val answers = actionPlanQuestionWriter.answersForActionPlanAndQuestions(actionPlan.id, listOf(question)).single()
    val currentAnswer = answers.currentAnswers.firstOrNull { it.headerId == actionId }
      ?: throw NotFoundException("Action not found with id=$actionId")

    actionPlanActivityRepository.deleteByActionPlanStepQuestionAnswerHeaderId(actionId)
    actionPlanQuestionWriter.write(
      answers,
      listOf(ActionPlanQuestionAnswers.Change.Delete(currentAnswer)),
      changedBy,
      changedAt,
      UUID.randomUUID(),
    )

    actionPlanQuestionWriter.reorderActionPlanActions(actionPlan.id)

    logger.info(
      "Successfully deleted action for referral={} with actionId={}",
      referralReference,
      actionId,
    )

    return ActionPlanActionResponse(
      success = true,
      message = "Action deleted successfully",
    )
  }

  fun findOrCreateByReferralId(referralId: UUID): ActionPlan = actionPlanDataFetcher.findOrCreateActionPlanForReferral(referralId)
}
