package uk.gov.justice.digital.hmpps.communitysupportapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanStepType
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferenceDataActionPlanStep
import java.util.UUID

interface ActionPlanStepRepository : JpaRepository<ReferenceDataActionPlanStep, UUID> {
  fun findAllByActionPlanTemplateIdOrderByOrderNumberAsc(actionPlanTemplateId: UUID): List<ReferenceDataActionPlanStep>

  fun findAllByStepTypeOrderByOrderNumberAsc(stepType: ActionPlanStepType): List<ReferenceDataActionPlanStep>

  @Query(
    """
    SELECT s FROM ReferenceDataActionPlanStep s
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
  ): List<ReferenceDataActionPlanStep>

  @Query(
    """
    SELECT s FROM ReferenceDataActionPlanStep s
    WHERE s.stepType = :stepType
    AND s.actionPlanTemplateId = (
      SELECT ap.actionPlanTemplateId FROM ActionPlan ap WHERE ap.referralId = :referralId
    )
    ORDER BY s.orderNumber ASC
    LIMIT 1
    """,
  )
  fun findStepByReferralIdAndStepType(
    @Param("referralId") referralId: UUID,
    @Param("stepType") stepType: ActionPlanStepType,
  ): ReferenceDataActionPlanStep?
}
