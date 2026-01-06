package uk.gov.justice.digital.hmpps.communitysupportapi.mapper

import uk.gov.justice.digital.hmpps.communitysupportapi.dto.nomis.NomisPersonDto
import uk.gov.justice.digital.hmpps.communitysupportapi.model.Person
import uk.gov.justice.digital.hmpps.communitysupportapi.model.PersonIdentifier

fun NomisPersonDto.toDomain(): Person = Person(identifier = PersonIdentifier.PrisonerNumber(prisonerNumber))
