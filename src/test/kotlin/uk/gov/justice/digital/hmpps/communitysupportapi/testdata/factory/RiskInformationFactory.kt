package uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory

import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Referral
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.RiskInformation
import java.time.OffsetDateTime
import java.util.UUID

class RiskInformationFactory : TestEntityFactory<RiskInformation>() {
  private var id: UUID = UUID.randomUUID()
  private lateinit var referral: Referral

  private var riskSummaryWhoIsAtRisk: String? = null
  private var riskSummaryNatureOfRisk: String? = null
  private var riskSummaryRiskImminence: String? = null
  private var riskToSelfSuicide: String? = null
  private var riskToSelfSelfHarm: String? = null
  private var riskToSelfHostelSetting: String? = null
  private var riskToSelfVulnerability: String? = null
  private var additionalInformation: String? = null

  private var updatedAt: OffsetDateTime = OffsetDateTime.now()
  private lateinit var updatedBy: UUID

  fun withId(id: UUID) = apply { this.id = id }
  fun withReferral(referral: Referral) = apply { this.referral = referral }

  fun withRiskSummaryWhoIsAtRisk(value: String?) = apply { this.riskSummaryWhoIsAtRisk = value }
  fun withRiskSummaryNatureOfRisk(value: String?) = apply { this.riskSummaryNatureOfRisk = value }
  fun withRiskSummaryRiskImminence(value: String?) = apply { this.riskSummaryRiskImminence = value }
  fun withRiskToSelfSuicide(value: String?) = apply { this.riskToSelfSuicide = value }
  fun withRiskToSelfSelfHarm(value: String?) = apply { this.riskToSelfSelfHarm = value }
  fun withRiskToSelfHostelSetting(value: String?) = apply { this.riskToSelfHostelSetting = value }
  fun withRiskToSelfVulnerability(value: String?) = apply { this.riskToSelfVulnerability = value }
  fun withAdditionalInformation(value: String?) = apply { this.additionalInformation = value }

  fun withUpdatedAt(updatedAt: OffsetDateTime) = apply { this.updatedAt = updatedAt }
  fun withUpdatedBy(userId: UUID) = apply { this.updatedBy = userId }

  override fun create(): RiskInformation = RiskInformation(
    id = id,
    referralId = referral.id,
    riskSummaryWhoIsAtRisk = riskSummaryWhoIsAtRisk,
    riskSummaryNatureOfRisk = riskSummaryNatureOfRisk,
    riskSummaryRiskImminence = riskSummaryRiskImminence,
    riskToSelfSuicide = riskToSelfSuicide,
    riskToSelfHarm = riskToSelfSelfHarm,
    riskToSelfHostelSetting = riskToSelfHostelSetting,
    riskToSelfVulnerability = riskToSelfVulnerability,
    additionalInformation = additionalInformation,
    updatedAt = updatedAt,
    updatedBy = updatedBy,
  )
}
