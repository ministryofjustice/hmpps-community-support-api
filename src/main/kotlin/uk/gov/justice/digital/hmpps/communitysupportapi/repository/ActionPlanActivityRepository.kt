package uk.gov.justice.digital.hmpps.communitysupportapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanActivity
import java.util.UUID

interface ActionPlanActivityRepository : JpaRepository<ActionPlanActivity, UUID> {
  fun findAllByActionPlanStepQuestionIdIn(actionPlanStepQuestionIds: Collection<UUID>): List<ActionPlanActivity>
}
