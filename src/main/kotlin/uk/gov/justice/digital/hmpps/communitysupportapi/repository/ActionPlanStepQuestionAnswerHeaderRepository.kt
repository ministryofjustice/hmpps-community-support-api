package uk.gov.justice.digital.hmpps.communitysupportapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanStepQuestionAnswerHeader
import java.util.UUID

interface ActionPlanStepQuestionAnswerHeaderRepository : JpaRepository<ActionPlanStepQuestionAnswerHeader, UUID> {
  fun findAllByActionPlanIdAndDeletedAtIsNull(actionPlanId: UUID): List<ActionPlanStepQuestionAnswerHeader>
  fun findAllByActionPlanStepQuestionIdIn(actionPlanStepQuestionIds: List<UUID>): List<ActionPlanStepQuestionAnswerHeader>
  // Find the (possibly null) ActionPlanStepQuestionAnswerHeader for a questionId and actionPlanId

  @Query(
    """
    SELECT h FROM ActionPlanStepQuestionAnswerHeader h
    WHERE h.actionPlanId = :actionPlanId AND h.actionPlanStepQuestionId = :questionId
    LIMIT 1
  """,
  )
  fun findByActionPlanIdAndQuestionId(actionPlanId: UUID, questionId: UUID): ActionPlanStepQuestionAnswerHeader?
}
