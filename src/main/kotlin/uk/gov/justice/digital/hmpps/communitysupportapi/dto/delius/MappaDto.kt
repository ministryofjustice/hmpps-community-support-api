package uk.gov.justice.digital.hmpps.communitysupportapi.dto.delius

import uk.gov.justice.digital.hmpps.communitysupportapi.dto.CodeDescriptionDto
import java.time.LocalDate

data class MappaDto(
  val level: Int? = null,
  val levelDescription: String? = null,
  val category: Int? = null,
  val categoryDescription: String? = null,
  val startDate: LocalDate? = null,
  val reviewDate: LocalDate? = null,
  val team: CodeDescriptionDto? = null,
  val officer: StaffDto? = null,
  val probationArea: CodeDescriptionDto? = null,
  val notes: String? = null,
)
