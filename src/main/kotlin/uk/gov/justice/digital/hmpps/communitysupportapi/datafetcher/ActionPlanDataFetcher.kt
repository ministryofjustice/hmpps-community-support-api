package uk.gov.justice.digital.hmpps.communitysupportapi.datafetcher

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlan
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanEvent
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanStep
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanStepType
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Referral
import uk.gov.justice.digital.hmpps.communitysupportapi.exception.NotFoundException
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ActionPlanEventRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ActionPlanRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ActionPlanStepQuestionAnswerDetailsRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ActionPlanStepQuestionRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ActionPlanStepRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ActionPlanTemplateRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralRepository
import java.util.UUID

@Component
class ActionPlanDataFetcher(
  val referralRepository: ReferralRepository,
  val actionPlanRepository: ActionPlanRepository,
  val actionPlanTemplateRepository: ActionPlanTemplateRepository,
  val actionPlanEventRepository: ActionPlanEventRepository,
  val actionPlanStepRepository: ActionPlanStepRepository,
  val actionPlanStepQuestionRepository: ActionPlanStepQuestionRepository,
  val actionPlanStepQuestionAnswerDetailsRepository: ActionPlanStepQuestionAnswerDetailsRepository,
) {
  companion object {
    private val logger = LoggerFactory.getLogger(ActionPlanDataFetcher::class.java)
  }

  fun getActionPlanDataForReferral(referralReference: String): ActionPlanData {
    logger.info("Retrieving action plan data for referral: {}", referralReference)
    val (referral, actionPlan) = getReferralAndActionPlanForReferral(referralReference)
    val allSteps = actionPlanStepRepository.findAllByActionPlanTemplateIdOrderByOrderNumberAsc(actionPlan.actionPlanTemplateId)
    val needSteps = allSteps.filter { it.stepType == ActionPlanStepType.NEED }

    return ActionPlanData(
      actionPlan,
      referral,
      needSteps,
    )
  }

  private fun getActionPlanStepData(
    referralReference: String,
    stepType: ActionPlanStepType,
    stepDescription: String,
  ): ActionPlanStepData {
    val (referral, actionPlan) = getReferralAndActionPlanForReferral(referralReference)
    val step = actionPlanStepRepository
      .findAllByActionPlanTemplateIdOrderByOrderNumberAsc(actionPlan.actionPlanTemplateId)
      .firstOrNull { it.stepType == stepType }
      ?: throw NotFoundException("No $stepDescription step found for referral $referralReference")

    return ActionPlanStepData(actionPlan, referral, step)
  }

  fun getSessionDeliveryDataForReferral(referralReference: String): SessionDeliveryData {
    val data = getActionPlanStepData(referralReference, ActionPlanStepType.SESSION_DELIVERY, "SESSION_DELIVERY")
    return SessionDeliveryData(data.actionPlan, data.referral, data.step)
  }

  fun getRiskAndAdjustmentsDataForReferral(referralReference: String): RiskAndAdjustmentsData {
    val data = getActionPlanStepData(referralReference, ActionPlanStepType.RISK_AND_ADJUSTMENTS, "risk and adjustments")
    return RiskAndAdjustmentsData(data.actionPlan, data.referral, data.step)
  }

  fun getConfirmServiceEndDateForReferral(referralReference: String): ConfirmServiceEndDateData {
    val data = getActionPlanStepData(referralReference, ActionPlanStepType.SERVICE_END_DATE_CHECK, "service end date check")
    return ConfirmServiceEndDateData(data.actionPlan, data.referral, data.step)
  }

  fun getUpdateServiceEndDateForReferral(referralReference: String): UpdateServiceEndDateData {
    val data = getActionPlanStepData(referralReference, ActionPlanStepType.CHANGE_SERVICE_END_DATE, "change service end date")
    return UpdateServiceEndDateData(data.actionPlan, data.referral, data.step)
  }

  fun getPersonInvolvementForReferral(referralReference: String): UpdateServiceEndDateData {
    val data = getActionPlanStepData(referralReference, ActionPlanStepType.USER_INVOLVEMENT, "person involvement")
    return UpdateServiceEndDateData(data.actionPlan, data.referral, data.step)
  }

  fun getServiceDeliveryDetailsDataForReferral(referralReference: String): ServiceDeliveryDetailsData {
    val (referral, actionPlan) = getReferralAndActionPlanForReferral(referralReference)
    val allSteps = actionPlanStepRepository.findAllByActionPlanTemplateIdOrderByOrderNumberAsc(actionPlan.actionPlanTemplateId)
    val sessionDeliveryStep = allSteps
      .firstOrNull { it.stepType == ActionPlanStepType.SESSION_DELIVERY }
      ?: throw NotFoundException("No session delivery step found for referral $referralReference")
    val riskAndAdjustmentsStep = allSteps
      .firstOrNull { it.stepType == ActionPlanStepType.RISK_AND_ADJUSTMENTS }
      ?: throw NotFoundException("No risk and adjustments step found for referral $referralReference")
    val serviceEndDateCheckStep = allSteps
      .firstOrNull { it.stepType == ActionPlanStepType.SERVICE_END_DATE_CHECK }
      ?: throw NotFoundException("No service end date check step found for referral $referralReference")
    val changeServiceEndDateStep = allSteps.firstOrNull { it.stepType == ActionPlanStepType.CHANGE_SERVICE_END_DATE }
      ?.takeIf {
        isServiceEndDateChanged(
          actionPlanId = actionPlan.id,
          serviceEndDateCheckStepId = serviceEndDateCheckStep.id,
        )
      }
    val userInvolvementStep = allSteps
      .firstOrNull { it.stepType == ActionPlanStepType.USER_INVOLVEMENT }
      ?: throw NotFoundException("No person involvement step found for referral $referralReference")

    return ServiceDeliveryDetailsData(
      actionPlan = actionPlan,
      referral = referral,
      steps = listOfNotNull(
        sessionDeliveryStep,
        riskAndAdjustmentsStep,
        serviceEndDateCheckStep,
        changeServiceEndDateStep,
        userInvolvementStep,
      ),
    )
  }

  private fun isServiceEndDateChanged(
    actionPlanId: UUID,
    serviceEndDateCheckStepId: UUID,
  ): Boolean {
    val confirmQuestionId = actionPlanStepQuestionRepository
      .findAllByActionPlanStepIdOrderByOrderNumberAsc(serviceEndDateCheckStepId)
      .singleOrNull()
      ?.id
      ?: return false

    return actionPlanStepQuestionAnswerDetailsRepository
      .getMostRecentAnswersForActionPlanQuestion(confirmQuestionId, actionPlanId)
      .any { it.content?.trim()?.equals("NO", ignoreCase = true) == true }
  }

  private fun getReferralAndActionPlanForReferral(referralReference: String): Pair<Referral, ActionPlan> {
    val referral = findReferralByReference(referralReference)
    val actionPlan = findOrCreateActionPlanForReferral(referral.id)
    return referral to actionPlan
  }

  fun findOrCreateActionPlanForReferral(referralId: UUID): ActionPlan = actionPlanRepository.findByReferralId(referralId)
    ?: createActionPlanForReferral(referralId)

  private fun findReferralByReference(referralReference: String): Referral = referralRepository
    .findByReferenceNumber(referralReference)
    .firstOrNull()
    ?: throw NotFoundException("Referral not found for reference $referralReference")

  private fun createActionPlanForReferral(referralId: UUID): ActionPlan {
    val actionPlanTemplate = actionPlanTemplateRepository.findFirstByActiveGlobalTrueOrderByIdAsc()

    if (actionPlanTemplate == null) {
      logger.warn("No active global action plan template found for referral ID: $referralId")
      throw NotFoundException("No active global action plan template found")
    }

    logger.info("Creating new Action Plan for referral ID: $referralId using template ID: ${actionPlanTemplate.id}")
    val actionPlan = ActionPlan.forReferral(actionPlanTemplate.id, referralId)
    actionPlanRepository.save(actionPlan)

    val actionPlanEvent = ActionPlanEvent.actionPlanCreatedEventForActionPlan(actionPlan.id)
    actionPlanEventRepository.save(actionPlanEvent)

    return actionPlan
  }
}

private data class ActionPlanStepData(
  val actionPlan: ActionPlan,
  val referral: Referral,
  val step: ActionPlanStep,
)

data class ActionPlanData(
  val actionPlan: ActionPlan,
  val referral: Referral,
  val needSteps: List<ActionPlanStep>,
)

data class SessionDeliveryData(
  val actionPlan: ActionPlan,
  val referral: Referral,
  val step: ActionPlanStep,
)

data class RiskAndAdjustmentsData(
  val actionPlan: ActionPlan,
  val referral: Referral,
  val step: ActionPlanStep,
)

data class ConfirmServiceEndDateData(
  val actionPlan: ActionPlan,
  val referral: Referral,
  val step: ActionPlanStep,
)

data class UpdateServiceEndDateData(
  val actionPlan: ActionPlan,
  val referral: Referral,
  val step: ActionPlanStep,
)

data class ServiceDeliveryDetailsData(
  val actionPlan: ActionPlan,
  val referral: Referral,
  val steps: List<ActionPlanStep>,
)
