package uk.gov.justice.digital.hmpps.communitysupportapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferenceDataActionPlanTemplate
import java.util.UUID

interface ActionPlanTemplateRepository : JpaRepository<ReferenceDataActionPlanTemplate, UUID> {
  @Query("SELECT DISTINCT a FROM ReferenceDataActionPlanTemplate a WHERE a.activeGlobal = true")
  fun getGlobalActionPlanTemplate(): ReferenceDataActionPlanTemplate?

  fun findFirstByActiveGlobalTrueOrderByIdAsc(): ReferenceDataActionPlanTemplate?
}
