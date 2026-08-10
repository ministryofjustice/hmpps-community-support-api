package uk.gov.justice.digital.hmpps.communitysupportapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.CommunityServiceProvider
import java.util.UUID

interface CommunityServiceProviderRepository : JpaRepository<CommunityServiceProvider, UUID> {
  @Query("SELECT rpa.communityServiceProvider FROM ReferralProviderAssignment rpa WHERE rpa.referral.id = :referralId")
  fun findByReferralId(@Param("referralId") referralId: UUID): CommunityServiceProvider?
}
