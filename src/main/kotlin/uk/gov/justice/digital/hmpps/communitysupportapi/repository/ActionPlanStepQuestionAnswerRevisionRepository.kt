package uk.gov.justice.digital.hmpps.communitysupportapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanStepQuestionAnswerRevision
import java.util.UUID

interface ActionPlanStepQuestionAnswerRevisionRepository : JpaRepository<ActionPlanStepQuestionAnswerRevision, UUID> {
  fun findAllByActionPlanStepQuestionAnswerIdIn(actionPlanStepQuestionAnswerIds: Collection<UUID>): List<ActionPlanStepQuestionAnswerRevision>

  @Query(
    """
    SELECT r FROM ActionPlanStepQuestionAnswerRevision r
    WHERE r.actionPlanStepQuestionAnswerId IN :answerIds
    AND r.revisionNumber = (
      SELECT MAX(r2.revisionNumber)
      FROM ActionPlanStepQuestionAnswerRevision r2
      WHERE r2.actionPlanStepQuestionAnswerId = r.actionPlanStepQuestionAnswerId
    )
    """,
  )
  fun findLatestRevisionsByAnswerIdIn(
    @Param("answerIds") answerIds: Collection<UUID>,
  ): List<ActionPlanStepQuestionAnswerRevision>
}
