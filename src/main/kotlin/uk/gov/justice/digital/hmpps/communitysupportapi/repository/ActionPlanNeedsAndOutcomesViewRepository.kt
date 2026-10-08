package uk.gov.justice.digital.hmpps.communitysupportapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanNeedsAndOutcomesView
import java.util.UUID

interface ActionPlanNeedsAndOutcomesViewRepository :
  JpaRepository<ActionPlanNeedsAndOutcomesView, UUID>,
  JpaSpecificationExecutor<ActionPlanNeedsAndOutcomesView>
