package uk.gov.justice.digital.hmpps.communitysupportapi.model

import java.time.LocalDate

data class Person(
  val identifier: PersonIdentifier,
  val firstName: String,
  val lastName: String,
  val dateOfBirth: LocalDate,
  val sex: String = "Unknown",
)
