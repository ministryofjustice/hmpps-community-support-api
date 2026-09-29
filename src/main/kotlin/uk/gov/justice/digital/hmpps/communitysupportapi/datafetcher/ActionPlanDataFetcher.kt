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
) {
  companion object {
    private val logger = LoggerFactory.getLogger(ActionPlanDataFetcher::class.java)
  }

  fun getActionPlanDataForReferral(referralReference: String): ActionPlanData {
    logger.info("Retrieving action plan data for referral: {}", referralReference)
    val referral = findReferralByReference(referralReference)

    val actionPlan = findOrCreateActionPlanForReferral(referral.id)

    val allSteps = actionPlanStepRepository.findAllByActionPlanTemplateIdOrderByOrderNumberAsc(actionPlan.actionPlanTemplateId)
    val needSteps = allSteps.filter { it.stepType == ActionPlanStepType.NEED }

    return ActionPlanData(
      actionPlan,
      referral,
      needSteps,
    )
  }

  fun getSessionDeliveryDataForReferral(referralReference: String): SessionDeliveryData {
    val referral = findReferralByReference(referralReference)
    val actionPlan = findOrCreateActionPlanForReferral(referral.id)
    val step = actionPlanStepRepository
      .findAllByActionPlanTemplateIdOrderByOrderNumberAsc(actionPlan.actionPlanTemplateId)
      .firstOrNull { it.stepType == ActionPlanStepType.SESSION_DELIVERY }
      ?: throw NotFoundException("No SESSION_DELIVERY step found for referral $referralReference")

    return SessionDeliveryData(actionPlan, referral, step)
  }

  fun getRiskAndAdjustmentsDataForReferral(referralReference: String): RiskAndAdjustmentsData {
    val referral = findReferralByReference(referralReference)
    val actionPlan = findOrCreateActionPlanForReferral(referral.id)
    val steps = actionPlanStepRepository
      .findAllByActionPlanTemplateIdOrderByOrderNumberAsc(actionPlan.actionPlanTemplateId)
      .filter { it.stepType == ActionPlanStepType.RISK_AND_ADJUSTMENTS }

    if (steps.isEmpty()) {
      throw NotFoundException("No risk and adjustments step found for referral $referralReference")
    }

    return RiskAndAdjustmentsData(actionPlan, referral, steps)
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
  val steps: List<ActionPlanStep>,
)
