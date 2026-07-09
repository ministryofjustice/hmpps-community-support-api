package uk.gov.justice.digital.hmpps.communitysupportapi.dto.arns

import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls
import java.time.LocalDateTime

data class ArnsRoshRiskDto(
  val riskToSelf: ArnsRoshRiskToSelfDto,
  val otherRisks: ArnsOtherRoshRisksDto,
  val summary: ArnsRiskRoshSummaryDto,
  val assessedOn: LocalDateTime? = null,
)

data class ArnsRoshRiskToSelfDto(
  val suicide: ArnsRiskDto? = null,
  val selfHarm: ArnsRiskDto? = null,
  val custody: ArnsRiskDto? = null,
  val hostelSetting: ArnsRiskDto? = null,
  val vulnerability: ArnsRiskDto? = null,
)

data class ArnsRiskDto(
  val risk: String? = null,
  val previous: String? = null,
  val previousConcernsText: String? = null,
  val current: String? = null,
  val currentConcernsText: String? = null,
)

data class ArnsOtherRoshRisksDto(
  val escapeOrAbscond: String? = null,
  val controlIssuesDisruptiveBehaviour: String? = null,
  val breachOfTrust: String? = null,
  val riskToOtherPrisoners: String? = null,
)

data class ArnsRiskRoshSummaryDto(
  val whoIsAtRisk: String? = null,
  val natureOfRisk: String? = null,
  val riskImminence: String? = null,
  val riskIncreaseFactors: String? = null,
  val riskMitigationFactors: String? = null,
  val analysisOfRiskFactors: String? = null,
  @JsonSetter(nulls = Nulls.AS_EMPTY)
  val riskInCommunity: Map<String, List<String>> = emptyMap(),
  @JsonSetter(nulls = Nulls.AS_EMPTY)
  val riskInCustody: Map<String, List<String>> = emptyMap(),
  val overallRiskLevel: String? = null,
)
