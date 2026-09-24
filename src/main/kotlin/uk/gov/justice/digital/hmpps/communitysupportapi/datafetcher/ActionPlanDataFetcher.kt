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
    val referral = referralRepository.findByReferenceNumber(referralReference).firstOrNull()

    if (referral == null) {
      logger.warn("Referral not found for reference $referralReference")
      throw NotFoundException("Referral not found for reference $referralReference")
    }

    val actionPlan = findOrCreateActionPlanForReferral(referral.id)

    val allSteps = actionPlanStepRepository.findAllByActionPlanTemplateIdOrderByOrderNumberAsc(actionPlan.actionPlanTemplateId)

    val sessionDeliveryStep = allSteps.firstOrNull { it.stepType == ActionPlanStepType.SESSION_DELIVERY }

    if (sessionDeliveryStep == null) {
      logger.warn("No Session Delivery Step found for referral $referralReference, action plan ID: ${actionPlan.id}")
      throw NotFoundException("No SESSION_DELIVERY step found for referral $referralReference")
    }

    val needSteps = allSteps.filter { it.stepType == ActionPlanStepType.NEED }

    if (needSteps.isEmpty()) {
      logger.warn("No Needs Steps found for action plan template ID: ${actionPlan.actionPlanTemplateId}")
      throw NotFoundException("No NEEDS steps found for action plan template ID: ${actionPlan.actionPlanTemplateId}")
    }

    return ActionPlanData(
      actionPlan,
      referral,
      sessionDeliveryStep,
      needSteps,
    )
  }

  private fun findOrCreateActionPlanForReferral(referralId: UUID): ActionPlan = actionPlanRepository.findByReferralId(referralId)
    ?: createActionPlanForReferral(referralId)

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
  val sessionDeliveryStep: ActionPlanStep,
  val needSteps: List<ActionPlanStep>,
)
