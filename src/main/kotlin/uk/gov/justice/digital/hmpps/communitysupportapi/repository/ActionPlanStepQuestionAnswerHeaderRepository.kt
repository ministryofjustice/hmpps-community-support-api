package uk.gov.justice.digital.hmpps.communitysupportapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanStepQuestionAnswerHeader
import java.util.UUID

interface ActionPlanStepQuestionAnswerHeaderRepository : JpaRepository<ActionPlanStepQuestionAnswerHeader, UUID> {
  fun findAllByActionPlanIdAndDeletedAtIsNull(actionPlanId: UUID): List<ActionPlanStepQuestionAnswerHeader>

  fun findByIdAndActionPlanIdAndActionPlanStepQuestionIdAndDeletedAtIsNull(
    id: UUID,
    actionPlanId: UUID,
    actionPlanStepQuestionId: UUID,
  ): ActionPlanStepQuestionAnswerHeader?

  @Query(
    """
  select h
  from ActionPlanStepQuestionAnswerHeader h
  where h.actionPlan.id = :actionPlanId
    and h.actionPlanStepQuestion.id in :questionIds
    and h.deletedAt is null
  order by h.actionPlanStepQuestion.id asc, h.orderNumber asc
  """,
  )
  fun findActiveByPlanAndQuestionIds(
    actionPlanId: UUID,
    questionIds: Collection<UUID>,
  ): List<ActionPlanStepQuestionAnswerHeader>
}
