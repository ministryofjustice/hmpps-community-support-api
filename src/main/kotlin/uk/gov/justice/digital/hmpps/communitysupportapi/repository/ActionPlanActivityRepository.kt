package uk.gov.justice.digital.hmpps.communitysupportapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanActivity
import java.util.UUID

interface ActionPlanActivityRepository : JpaRepository<ActionPlanActivity, UUID> {
  fun findByActionPlanStepQuestionAnswerHeaderId(headerId: UUID): List<ActionPlanActivity>

  fun findAllByActionPlanStepQuestionAnswerHeaderIdIn(actionPlanStepQuestionAnswerHeaderIds: Collection<UUID>): List<ActionPlanActivity>

  fun deleteByActionPlanStepQuestionAnswerHeaderId(headerId: UUID)
}
