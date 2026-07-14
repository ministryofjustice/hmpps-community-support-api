package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import uk.gov.justice.digital.hmpps.communitysupportapi.entity.RiskInformation
import java.util.UUID

data class CommunitySupportRiskInformationDto(
  val id: UUID,
  val referralId: UUID,
  val riskSummaryWhoIsAtRisk: String? = null,
  val riskSummaryNatureOfRisk: String? = null,
  val riskSummaryRiskImminence: String? = null,
  val riskToSelfSuicide: String? = null,
  val riskToSelfSelfHarm: String? = null,
  val riskToSelfHostelSetting: String? = null,
  val riskToSelfVulnerability: String? = null,
  val additionalInformation: String? = null,
) {
  companion object {
    fun from(riskInformation: RiskInformation): CommunitySupportRiskInformationDto = CommunitySupportRiskInformationDto(
      id = riskInformation.id,
      referralId = riskInformation.referralId,
      riskSummaryWhoIsAtRisk = riskInformation.riskSummaryWhoIsAtRisk,
      riskSummaryNatureOfRisk = riskInformation.riskSummaryNatureOfRisk,
      riskSummaryRiskImminence = riskInformation.riskSummaryRiskImminence,
      riskToSelfSuicide = riskInformation.riskToSelfSuicide,
      riskToSelfSelfHarm = riskInformation.riskToSelfHarm,
      riskToSelfHostelSetting = riskInformation.riskToSelfHostelSetting,
      riskToSelfVulnerability = riskInformation.riskToSelfVulnerability,
      additionalInformation = riskInformation.additionalInformation,
    )
  }
}
