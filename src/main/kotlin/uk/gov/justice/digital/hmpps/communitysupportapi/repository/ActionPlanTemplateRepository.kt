package uk.gov.justice.digital.hmpps.communitysupportapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanTemplate
import java.util.UUID

interface ActionPlanTemplateRepository : JpaRepository<ActionPlanTemplate, UUID> {
  @Query("SELECT a FROM ActionPlanTemplate a WHERE a.id = CAST('c191398c-9661-4983-bafb-be649d877183' AS java.util.UUID)")
  fun getGlobalActionPlanTemplate(): ActionPlanTemplate?
}
