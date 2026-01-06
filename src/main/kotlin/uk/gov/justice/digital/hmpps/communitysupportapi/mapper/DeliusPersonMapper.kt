package uk.gov.justice.digital.hmpps.communitysupportapi.mapper

import uk.gov.justice.digital.hmpps.communitysupportapi.dto.DeliusPersonDto
import uk.gov.justice.digital.hmpps.communitysupportapi.model.Person
import uk.gov.justice.digital.hmpps.communitysupportapi.model.PersonIdentifier

fun DeliusPersonDto.toDomain(): Person = Person(identifier = PersonIdentifier.Crn(crn!!))
