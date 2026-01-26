package uk.gov.justice.digital.hmpps.communitysupportapi.dto.delius

import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls
import java.time.LocalDate

data class OffenderAliasDto(
  val id: String? = null,
  val dateOfBirth: LocalDate? = null,
  val firstName: String? = null,
  @JsonSetter(nulls = Nulls.AS_EMPTY)
  val middleNames: List<String> = emptyList(),
  val surname: String? = null,
  val gender: String = "Unknown",
)
