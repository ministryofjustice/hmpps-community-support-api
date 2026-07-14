package uk.gov.justice.digital.hmpps.communitysupportapi.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "risk_information")
class RiskInformation(
  @Id
  @Column(name = "id")
  val id: UUID,

  @Column(name = "referral_id", nullable = false)
  val referralId: UUID,

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "referral_id", insertable = false, updatable = false)
  val referral: Referral? = null,

  @Column(name = "risk_summary_who_is_at_risk")
  val riskSummaryWhoIsAtRisk: String? = null,

  @Column(name = "risk_summary_nature_of_risk")
  val riskSummaryNatureOfRisk: String? = null,

  @Column(name = "risk_summary_risk_imminence")
  val riskSummaryRiskImminence: String? = null,

  @Column(name = "risk_to_self_suicide")
  val riskToSelfSuicide: String? = null,

  @Column(name = "risk_to_self_harm")
  val riskToSelfHarm: String? = null,

  @Column(name = "risk_to_self_hostel_setting")
  val riskToSelfHostelSetting: String? = null,

  @Column(name = "risk_to_self_vulnerability")
  val riskToSelfVulnerability: String? = null,

  @Column(name = "additional_information")
  val additionalInformation: String? = null,

  @Column(name = "updated_at", nullable = false)
  val updatedAt: OffsetDateTime,

  @Column(name = "updated_by", nullable = false)
  val updatedBy: UUID,
)
