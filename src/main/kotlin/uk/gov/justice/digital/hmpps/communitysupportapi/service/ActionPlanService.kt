package uk.gov.justice.digital.hmpps.communitysupportapi.service

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

  @Transactional
  fun findOrCreateByReferralId(referralId: UUID): ActionPlan = actionPlanRepository.findByReferralId(referralId)
    ?: createActionPlan(referralId)

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
