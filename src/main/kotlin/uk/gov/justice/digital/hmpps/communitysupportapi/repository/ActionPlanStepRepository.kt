package uk.gov.justice.digital.hmpps.communitysupportapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanStep
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanStepType
import java.util.UUID

interface ActionPlanStepRepository : JpaRepository<ActionPlanStep, UUID> {
  fun findAllByActionPlanTemplateIdOrderByOrderNumberAsc(actionPlanTemplateId: UUID): List<ActionPlanStep>

  fun findAllByStepTypeOrderByOrderNumberAsc(stepType: ActionPlanStepType): List<ActionPlanStep>

  @Query(
    """
    SELECT s FROM ActionPlanStep s
    WHERE s.stepType = :stepType
    AND s.actionPlanTemplateId = (
      SELECT ap.actionPlanTemplateId FROM ActionPlan ap WHERE ap.referralId = :referralId
    )
    ORDER BY s.orderNumber ASC
    """,
  )
  fun findNeedStepsByReferralId(
    @Param("referralId") referralId: UUID,
    @Param("stepType") stepType: ActionPlanStepType = ActionPlanStepType.NEED,
  ): List<ActionPlanStep>

  @Query(
    """
    SELECT s FROM ActionPlanStep s
    WHERE s.stepType = ActionPlanStepType.SESSION_DELIVERY
    AND s.actionPlanTemplateId = (
      SELECT ap.actionPlanTemplateId FROM ActionPlan ap WHERE ap.referralId = :referralId
    )
    ORDER BY s.orderNumber ASC
    """,
  )
  fun findSessionDeliveryStepsByReferralId(
    @Param("referralId") referralId: UUID,
  ): List<ActionPlanStep>
}
