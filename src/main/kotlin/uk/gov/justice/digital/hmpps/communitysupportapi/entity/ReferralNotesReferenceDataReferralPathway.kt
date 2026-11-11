package uk.gov.justice.digital.hmpps.communitysupportapi.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "referral_notes_reference_data_referral_pathway")
class ReferralNotesReferenceDataReferralPathway(
  @Id
  val id: UUID,

  @Column(name = "reference_data_referral_pathway_id", nullable = false)
  val referenceDataReferralPathwayId: UUID,

  @Column(name = "referral_notes_id", nullable = false)
  val referralNoteId: UUID,
) {
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "reference_data_referral_pathway_id", insertable = false, updatable = false)
  val referenceDataReferralPathway: ReferenceDataReferralPathway? = null
}
