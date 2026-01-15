package uk.gov.justice.digital.hmpps.communitysupportapi.model

sealed class PersonIdentifier {
  data class Crn(val value: String) : PersonIdentifier()
  data class PrisonerNumber(val value: String) : PersonIdentifier()
}
