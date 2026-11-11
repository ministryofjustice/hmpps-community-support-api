package uk.gov.justice.digital.hmpps.communitysupportapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralNonContactSession
import java.util.UUID

interface ReferralNonContactSessionRepository : JpaRepository<ReferralNonContactSession, UUID> {
  fun findAllByReferralIdAndDeletedAtIsNullOrderByCreatedAtDesc(referralId: UUID): List<ReferralNonContactSession>
}
