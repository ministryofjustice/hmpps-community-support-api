package uk.gov.justice.digital.hmpps.communitysupportapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanStep
import java.util.UUID

interface ActionPlanStepRepository : JpaRepository<ActionPlanStep, UUID> {
  fun findAllByActionPlanTemplateIdOrderByOrderNumberAsc(actionPlanTemplateId: UUID): List<ActionPlanStep>
}
