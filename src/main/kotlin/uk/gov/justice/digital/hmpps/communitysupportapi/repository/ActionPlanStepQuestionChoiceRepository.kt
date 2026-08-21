package uk.gov.justice.digital.hmpps.communitysupportapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanStepQuestionChoice
import java.util.UUID

interface ActionPlanStepQuestionChoiceRepository : JpaRepository<ActionPlanStepQuestionChoice, UUID> {
  fun findByActionPlanStepQuestionIdOrderByOrderNumberAsc(actionPlanStepQuestionId: UUID): List<ActionPlanStepQuestionChoice>

  fun findAllByActionPlanStepQuestionIdIn(actionPlanStepQuestionIds: Collection<UUID>): List<ActionPlanStepQuestionChoice>
}
