package uk.gov.justice.digital.hmpps.communitysupportapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanStepQuestionAnswerHeader
import java.util.UUID

interface ActionPlanStepQuestionAnswerHeaderRepository : JpaRepository<ActionPlanStepQuestionAnswerHeader, UUID> {
  fun findAllByActionPlanIdAndDeletedAtIsNull(actionPlanId: UUID): List<ActionPlanStepQuestionAnswerHeader>
  fun findAllByActionPlanStepQuestionIdIn(actionPlanStepQuestionIds: List<UUID>): List<ActionPlanStepQuestionAnswerHeader>
}
