package uk.gov.justice.digital.hmpps.communitysupportapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralWithdrawalDetails
import java.util.UUID

@Repository
interface ReferralWithdrawalDetailsRepository : JpaRepository<ReferralWithdrawalDetails, UUID> {
  fun findByReferralId(referralId: UUID): ReferralWithdrawalDetails?
}
