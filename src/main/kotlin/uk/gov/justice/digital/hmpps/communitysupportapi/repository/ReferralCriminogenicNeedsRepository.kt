package uk.gov.justice.digital.hmpps.communitysupportapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralCriminogenicNeeds
import java.util.UUID

@Repository
interface ReferralCriminogenicNeedsRepository : JpaRepository<ReferralCriminogenicNeeds, UUID> {
  fun findByReferralId(referralId: UUID): ReferralCriminogenicNeeds?
}
