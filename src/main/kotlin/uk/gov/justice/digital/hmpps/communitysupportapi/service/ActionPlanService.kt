package uk.gov.justice.digital.hmpps.communitysupportapi.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.ActionPlanSummaryDto
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlan
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanEvent
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanQuestionType
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanStepType
import uk.gov.justice.digital.hmpps.communitysupportapi.exception.NotFoundException
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ActionPlanEventRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ActionPlanRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ActionPlanStepQuestionAnswerRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ActionPlanStepQuestionAnswerRevisionRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ActionPlanStepQuestionRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ActionPlanStepRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ActionPlanTemplateRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.NeedRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.PersonRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralRepository
import java.util.UUID

@Service
class ActionPlanService(
  private val actionPlanRepository: ActionPlanRepository,
  private val actionPlanEventRepository: ActionPlanEventRepository,
  private val actionPlanTemplateRepository: ActionPlanTemplateRepository,
  private val actionPlanStepRepository: ActionPlanStepRepository,
  private val actionPlanStepQuestionRepository: ActionPlanStepQuestionRepository,
  private val actionPlanStepQuestionAnswerRepository: ActionPlanStepQuestionAnswerRepository,
  private val actionPlanStepQuestionAnswerRevisionRepository: ActionPlanStepQuestionAnswerRevisionRepository,
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

  private fun createForReferral(referralId: UUID): ActionPlan {
    val existingActionPlan = actionPlanRepository.findByReferralId(referralId)
    if (existingActionPlan != null) {
      logger.warn("Action plan already exists for referral {}, skipping creation", referralId)
      return existingActionPlan
    }

    val actionPlanTemplate = actionPlanTemplateRepository.findFirstByOrderByIdAsc()
      ?: throw NotFoundException("No action plan template found")

    val actionPlan = ActionPlan.forReferral(actionPlanTemplate.id, referralId)
    actionPlanRepository.save(actionPlan)

    val actionPlanEvent = ActionPlanEvent.actionPlanCreatedEventForActionPlan(actionPlan.id)
    actionPlanEventRepository.save(actionPlanEvent)

    return actionPlan
  }

  private fun getOutcomesByNeedIdForActionPlan(actionPlanId: UUID, actionPlanTemplateId: UUID): Map<UUID, List<String>> {
    val needSteps = actionPlanStepRepository
      .findAllByActionPlanTemplateIdOrderByOrderNumberAsc(actionPlanTemplateId)
      .filter { it.stepType == ActionPlanStepType.NEED && it.needId != null }
    if (needSteps.isEmpty()) {
      return emptyMap()
    }

    val stepById = needSteps.associateBy { it.id }
    val questionsByStepId = actionPlanStepQuestionRepository
      .findAllByActionPlanStepIdInOrderByOrderNumberAsc(stepById.keys)
      .filter { it.questionType == ActionPlanQuestionType.OUTCOME }
      .groupBy { it.actionPlanStepId }
    val questionById = questionsByStepId.values.flatten().associateBy { it.id }
    if (questionById.isEmpty()) {
      return emptyMap()
    }

    val answers = actionPlanStepQuestionAnswerRepository
      .findAllByActionPlanIdAndDeletedAtIsNull(actionPlanId)
      .filter { questionById.containsKey(it.actionPlanStepQuestionId) }
      .sortedBy { it.orderNumber }
    if (answers.isEmpty()) {
      return emptyMap()
    }

    val revisions = actionPlanStepQuestionAnswerRevisionRepository.findAllByActionPlanStepQuestionAnswerIdIn(answers.map { it.id })
    val latestRevisionByAnswerId = revisions
      .groupBy { it.actionPlanStepQuestionAnswerId }
      .mapValues { (_, revisionItems) -> revisionItems.maxByOrNull { it.revisionNumber } }

    return answers
      .mapNotNull { answer ->
        val question = questionById[answer.actionPlanStepQuestionId] ?: return@mapNotNull null
        val step = stepById[question.actionPlanStepId] ?: return@mapNotNull null
        val needId = step.needId ?: return@mapNotNull null
        val latestRevision = latestRevisionByAnswerId[answer.id] ?: return@mapNotNull null
        val content = latestRevision.content?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
        needId to content
      }
      .groupBy(keySelector = { it.first }, valueTransform = { it.second })
  }
}
