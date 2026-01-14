package uk.gov.justice.digital.hmpps.communitysupportapi.dto.nomis

import java.time.LocalDate
import java.time.LocalDateTime

data class IdentifiersDto(
  val type: String? = null,
  val value: String? = null,
  val issuedDate: LocalDate? = null,
  val issuedAuthorityText: String? = null,
  val createdDateTime: LocalDateTime? = null,
)
