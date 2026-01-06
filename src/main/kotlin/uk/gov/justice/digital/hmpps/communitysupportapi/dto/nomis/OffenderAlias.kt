package uk.gov.justice.digital.hmpps.communitysupportapi.dto.nomis

data class OffenderAlias(
  val id: String? = null,
  val dateOfBirth: String? = null,
  val firstName: String? = null,
  val middleNames: List<String> = emptyList(),
  val surname: String? = null,
  val gender: String? = null
)
