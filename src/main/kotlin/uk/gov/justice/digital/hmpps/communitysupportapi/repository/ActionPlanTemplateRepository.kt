package uk.gov.justice.digital.hmpps.communitysupportapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanTemplate
import java.util.UUID

interface ActionPlanTemplateRepository : JpaRepository<ActionPlanTemplate, UUID> {
  @Query("SELECT DISTINCT a FROM ActionPlanTemplate a WHERE a.activeGlobal = true")
  fun getGlobalActionPlanTemplate(): ActionPlanTemplate?

  fun findFirstByOrderByIdAsc(): ActionPlanTemplate?
}
