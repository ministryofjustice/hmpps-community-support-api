package uk.gov.justice.digital.hmpps.communitysupportapi.entity

import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "reference_data_referral_pathway_need")
class ReferenceDataReferralPathwayNeed(
  @Id
  val id: UUID,

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "reference_data_referral_pathway_id", nullable = false)
  val referenceDataReferralPathway: ReferenceDataReferralPathway,

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "reference_data_need_id", nullable = false)
  val referenceDataNeed: ReferenceDataNeed,
)
