package uk.gov.justice.digital.hmpps.communitysupportapi.dto.delius

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls

data class HighlightDto(
  @JsonSetter(nulls = Nulls.AS_EMPTY)
  val surname: List<String> = emptyList(),

  @JsonProperty("offenderAliases.surname")
  val offenderAliasesSurname: List<String>,
)
