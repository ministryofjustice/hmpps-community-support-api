package uk.gov.justice.digital.hmpps.communitysupportapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferenceDataCommunityServiceProvider
import java.util.UUID

interface CommunityServiceProviderRepository : JpaRepository<ReferenceDataCommunityServiceProvider, UUID> {
  @Query("SELECT rpa.referenceDataCommunityServiceProvider FROM ReferralProviderAssignment rpa WHERE rpa.referral.id = :referralId")
  fun findByReferralId(@Param("referralId") referralId: UUID): ReferenceDataCommunityServiceProvider?
}
