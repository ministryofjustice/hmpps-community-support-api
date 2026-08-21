package uk.gov.justice.digital.hmpps.communitysupportapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanStepQuestion
import java.util.UUID

interface ActionPlanStepQuestionRepository : JpaRepository<ActionPlanStepQuestion, UUID> {
  fun findAllByActionPlanStepIdInOrderByOrderNumberAsc(actionPlanStepIds: Collection<UUID>): List<ActionPlanStepQuestion>

  fun findAllByActionPlanStepIdOrderByOrderNumberAsc(actionPlanStepId: UUID): List<ActionPlanStepQuestion>
}
