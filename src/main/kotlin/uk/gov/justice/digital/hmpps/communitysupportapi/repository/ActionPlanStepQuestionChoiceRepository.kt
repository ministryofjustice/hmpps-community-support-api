package uk.gov.justice.digital.hmpps.communitysupportapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferenceDataActionPlanStepQuestionChoice
import java.util.UUID

interface ActionPlanStepQuestionChoiceRepository : JpaRepository<ReferenceDataActionPlanStepQuestionChoice, UUID> {
  fun findByActionPlanStepQuestionIdOrderByOrderNumberAsc(actionPlanStepQuestionId: UUID): List<ReferenceDataActionPlanStepQuestionChoice>

  fun findAllByActionPlanStepQuestionIdIn(actionPlanStepQuestionIds: Collection<UUID>): List<ReferenceDataActionPlanStepQuestionChoice>
}
