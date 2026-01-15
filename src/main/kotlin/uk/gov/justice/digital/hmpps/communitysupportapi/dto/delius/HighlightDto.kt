package uk.gov.justice.digital.hmpps.communitysupportapi.dto.delius

import com.fasterxml.jackson.annotation.JsonProperty

data class HighlightDto(
  val surname: List<String> = emptyList(),

  @JsonProperty("offenderAliases.surname")
  val offenderAliasesSurname: List<String>,
)
