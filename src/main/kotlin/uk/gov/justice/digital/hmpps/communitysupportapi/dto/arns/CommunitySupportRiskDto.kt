package uk.gov.justice.digital.hmpps.communitysupportapi.dto.arns

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class CommunitySupportRiskDto(
  val assessmentWithin12Months: Boolean,
  val assessedOn: String? = null,
  val riskToSelf: ArnsRiskConcernsToSelfDto? = null,
  val summary: ArnsRiskRoshSummaryDto? = null,
)
