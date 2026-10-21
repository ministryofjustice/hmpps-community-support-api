package uk.gov.justice.digital.hmpps.communitysupportapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferenceDataNeed
import java.util.UUID

interface NeedRepository : JpaRepository<ReferenceDataNeed, UUID> {
  fun findAllByIdInOrderByOrderNumberAsc(needIds: Collection<UUID>): List<ReferenceDataNeed>
  fun findAllByOrderByOrderNumberAsc(): List<ReferenceDataNeed>

  @Query(
    """
    select need
    from ReferenceDataNeed need, ReferralCriminogenicNeeds referralNeeds
    where referralNeeds.referral.id = :referralId
      and (
        (need.orderNumber = 1 and referralNeeds.hasAccommodationNeeds = true)
        or (need.orderNumber = 2 and referralNeeds.hasEmploymentEducationNeeds = true)
        or (need.orderNumber = 3 and referralNeeds.hasFinancialNeeds = true)
        or (need.orderNumber = 4 and referralNeeds.hasDrugUseNeeds = true)
        or (need.orderNumber = 5 and referralNeeds.hasAlcoholUseNeeds = true)
        or (need.orderNumber = 6 and referralNeeds.hasHealthWellbeingNeeds = true)
        or (need.orderNumber = 7 and referralNeeds.hasPersonalRelationshipsCommunityNeeds = true)
        or (need.orderNumber = 8 and referralNeeds.hasThinkingBehavioursAttitudeNeeds = true)
      )
    order by need.orderNumber asc
    """,
  )
  fun findSelectedForReferralIdOrderByOrderNumberAsc(@Param("referralId") referralId: UUID): List<ReferenceDataNeed>
}
