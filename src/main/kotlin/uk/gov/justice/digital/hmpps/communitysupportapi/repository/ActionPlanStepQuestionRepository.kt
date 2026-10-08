package uk.gov.justice.digital.hmpps.communitysupportapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferenceDataActionPlanStepQuestion
import java.util.UUID

interface ActionPlanStepQuestionRepository : JpaRepository<ReferenceDataActionPlanStepQuestion, UUID> {
  fun findAllByActionPlanStepIdInOrderByOrderNumberAsc(actionPlanStepIds: Collection<UUID>): List<ReferenceDataActionPlanStepQuestion>

  fun findAllByActionPlanStepIdOrderByOrderNumberAsc(actionPlanStepId: UUID): List<ReferenceDataActionPlanStepQuestion>
}
