package uk.gov.justice.digital.hmpps.communitysupportapi.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlan
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanEvent
import uk.gov.justice.digital.hmpps.communitysupportapi.exception.NotFoundException
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ActionPlanEventRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ActionPlanRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ActionPlanTemplateRepository
import java.util.UUID

@Service
class ActionPlanService(
  private val actionPlanRepository: ActionPlanRepository,
  private val actionPlanEventRepository: ActionPlanEventRepository,
  private val actionPlanTemplateRepository: ActionPlanTemplateRepository,
) {
  companion object {
    private val logger = LoggerFactory.getLogger(ActionPlanService::class.java)
  }

  @Transactional
  fun findOrCreateByReferralId(referralId: UUID): ActionPlan = actionPlanRepository.findByReferralId(referralId)
    ?: createActionPlan(referralId)

  @Transactional
  fun createForReferral(referralId: UUID) {
    val existingActionPlan = actionPlanRepository.findByReferralId(referralId)
    if (existingActionPlan != null) {
      logger.warn("Action plan already exists for referral {}, skipping creation", referralId)
      return
    }

    val actionPlanTemplate = actionPlanTemplateRepository.getGlobalActionPlanTemplate()
    val actionPlan = ActionPlan.forReferral(actionPlanTemplate!!.id, referralId)
    actionPlanRepository.save(actionPlan)

    val actionPlanEvent = ActionPlanEvent.actionPlanCreatedEventForActionPlan(actionPlan.id)
    actionPlanEventRepository.save(actionPlanEvent)
  }

  private fun createActionPlan(referralId: UUID): ActionPlan {
    val template = actionPlanTemplateRepository.findFirstByOrderByIdAsc()
      ?: throw NotFoundException("No action plan template found")

    val actionPlan = actionPlanRepository.save(
      ActionPlan.forReferral(
        actionPlanTemplateId = template.id,
        referralId = referralId,
      ),
    )

    actionPlanEventRepository.save(
      ActionPlanEvent.actionPlanCreatedEventForActionPlan(actionPlan.id),
    )

    return actionPlan
  }
}
