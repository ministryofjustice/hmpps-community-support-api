package uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory

import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralNotesReferenceDataReferralPathway
import java.util.UUID

class ReferralNotesReferenceDataReferralPathwayFactory : TestEntityFactory<ReferralNotesReferenceDataReferralPathway>() {
  private var id: UUID = UUID.randomUUID()
  private var referenceDataReferralPathwayId: UUID = UUID.randomUUID()
  private var referralNoteId: UUID = UUID.randomUUID()

  fun withId(id: UUID) = apply { this.id = id }
  fun withReferenceDataReferralPathwayId(id: UUID) = apply { this.referenceDataReferralPathwayId = id }
  fun withReferralNoteId(id: UUID) = apply { this.referralNoteId = id }

  override fun create(): ReferralNotesReferenceDataReferralPathway = ReferralNotesReferenceDataReferralPathway(
    id = id,
    referenceDataReferralPathwayId = referenceDataReferralPathwayId,
    referralNoteId = referralNoteId,
  )
}
