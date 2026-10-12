package uk.gov.justice.digital.hmpps.communitysupportapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanNeedsAndOutcomesView
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanNeedsAndOutcomesViewId
import java.util.UUID

@Repository
interface ActionPlanNeedsAndOutcomesViewRepository :
  JpaRepository<ActionPlanNeedsAndOutcomesView, ActionPlanNeedsAndOutcomesViewId>,
  JpaSpecificationExecutor<ActionPlanNeedsAndOutcomesView> {
  fun findByIdReferralIdOrderByDisplayOrderAsc(referralId: UUID): MutableList<ActionPlanNeedsAndOutcomesView>
}
