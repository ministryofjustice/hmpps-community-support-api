package uk.gov.justice.digital.hmpps.communitysupportapi.mapper

import uk.gov.justice.digital.hmpps.communitysupportapi.dto.PersonDto
import uk.gov.justice.digital.hmpps.communitysupportapi.model.Person
import uk.gov.justice.digital.hmpps.communitysupportapi.model.PersonIdentifier

fun Person.toDto(): PersonDto = PersonDto(
  personIdentifier = when (identifier) {
    is PersonIdentifier.Crn -> identifier.value
    is PersonIdentifier.PrisonerNumber -> identifier.value
  },
)
