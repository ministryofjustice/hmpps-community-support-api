package uk.gov.justice.digital.hmpps.communitysupportapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanTemplate
import java.util.UUID

interface ActionPlanTemplateRepository : JpaRepository<ActionPlanTemplate, UUID>
