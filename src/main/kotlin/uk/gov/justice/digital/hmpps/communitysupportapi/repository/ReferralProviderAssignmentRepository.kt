package uk.gov.justice.digital.hmpps.communitysupportapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralProviderAssignment
import java.util.UUID

interface ReferralProviderAssignmentRepository : JpaRepository<ReferralProviderAssignment, UUID> {
  fun findByReferralId(referralId: UUID): List<ReferralProviderAssignment>
}
