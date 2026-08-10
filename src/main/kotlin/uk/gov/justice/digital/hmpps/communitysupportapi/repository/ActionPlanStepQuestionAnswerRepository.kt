package uk.gov.justice.digital.hmpps.communitysupportapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanStepQuestionAnswer
import java.util.UUID

interface ActionPlanStepQuestionAnswerRepository : JpaRepository<ActionPlanStepQuestionAnswer, UUID> {
  fun findAllByActionPlanIdAndDeletedAtIsNull(actionPlanId: UUID): List<ActionPlanStepQuestionAnswer>
}
