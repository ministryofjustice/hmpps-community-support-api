package uk.gov.justice.digital.hmpps.communitysupportapi.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "reference_data_referral_pathway")
class ReferenceDataReferralPathway(
  @Id
  val id: UUID,

  @Column(name = "title", nullable = false)
  val title: String,

  @Column(name = "display_order", nullable = false)
  val displayOrder: Int,

  @OneToOne
  @JoinColumn(name = "id", referencedColumnName = "reference_data_referral_pathway_id", nullable = false)
  val referralPathwayNeed: ReferenceDataReferralPathwayNeed,
)
