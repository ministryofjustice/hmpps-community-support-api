package uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory

import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralNonContactSessionReferenceDataReferralPathway
import java.util.UUID

class ReferralNonContactSessionReferenceDataReferralPathwayFactory : TestEntityFactory<ReferralNonContactSessionReferenceDataReferralPathway>() {
  private var id: UUID = UUID.randomUUID()
  private var referenceDataReferralPathwayId: UUID = UUID.randomUUID()
  private var referralNonContactSessionId: UUID = UUID.randomUUID()

  fun withId(id: UUID) = apply { this.id = id }
  fun withReferenceDataReferralPathwayId(id: UUID) = apply { this.referenceDataReferralPathwayId = id }
  fun withReferralNonContactSessionId(id: UUID) = apply { this.referralNonContactSessionId = id }

  override fun create(): ReferralNonContactSessionReferenceDataReferralPathway = ReferralNonContactSessionReferenceDataReferralPathway(
    id = id,
    referenceDataReferralPathwayId = referenceDataReferralPathwayId,
    referralNonContactSessionId = referralNonContactSessionId,
  )
}
