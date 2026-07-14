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
  val referral: Referral,

  @Column(name = "risk_summary_who_is_at_risk")
  var riskSummaryWhoIsAtRisk: String? = null,

  @Column(name = "risk_summary_nature_of_risk")
  var riskSummaryNatureOfRisk: String? = null,

  @Column(name = "risk_summary_risk_imminence")
  var riskSummaryRiskImminence: String? = null,

  @Column(name = "risk_to_self_suicide")
  var riskToSelfSuicide: String? = null,

  @Column(name = "risk_to_self_harm")
  var riskToSelfHarm: String? = null,

  @Column(name = "risk_to_self_hostel_setting")
  var riskToSelfHostelSetting: String? = null,

  @Column(name = "risk_to_self_vulnerability")
  var riskToSelfVulnerability: String? = null,

  @Column(name = "additional_information")
  var additionalInformation: String? = null,

  @Column(name = "updated_at", nullable = false)
  var updatedAt: OffsetDateTime,

  @Column(name = "updated_by", nullable = false)
  var updatedBy: UUID,
)
