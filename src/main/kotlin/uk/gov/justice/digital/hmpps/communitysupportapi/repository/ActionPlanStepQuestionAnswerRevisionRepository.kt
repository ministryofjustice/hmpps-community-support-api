package uk.gov.justice.digital.hmpps.communitysupportapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanStepQuestionAnswerRevision
import java.util.UUID

interface ActionPlanStepQuestionAnswerRevisionRepository : JpaRepository<ActionPlanStepQuestionAnswerRevision, UUID> {
  fun findAllByActionPlanStepQuestionAnswerIdIn(actionPlanStepQuestionAnswerIds: Collection<UUID>): List<ActionPlanStepQuestionAnswerRevision>
}
