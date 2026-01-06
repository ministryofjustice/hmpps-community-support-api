package uk.gov.justice.digital.hmpps.communitysupportapi.dto.nomis

data class Mappa(
  val level: Int? = null,
  val levelDescription: String? = null,
  val category: Int? = null,
  val categoryDescription: String? = null,
  val startDate: String? = null,
  val reviewDate: String? = null,
  val team: Team? = null,
  val officer: Staff? = null,
  val probationArea: CodeDescription? = null,
  val notes: String? = null
)
