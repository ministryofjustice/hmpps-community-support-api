package uk.gov.justice.digital.hmpps.communitysupportapi.mapper

import uk.gov.justice.digital.hmpps.communitysupportapi.dto.PersonDto
import uk.gov.justice.digital.hmpps.communitysupportapi.util.parseDateOfBirth
import java.time.OffsetDateTime
import java.util.UUID
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Person as PersonEntity

fun PersonDto.toEntity(): PersonEntity {
  val person = PersonEntity(
    id = UUID.randomUUID(),
    identifier = personIdentifier!!,
    firstName = firstName,
    lastName = lastName,
    dateOfBirth = dateOfBirth.parseDateOfBirth(),
    gender = sex!!,
    createdAt = OffsetDateTime.now(),
    updatedAt = OffsetDateTime.now(),
    prisonNumbers = prisonNumbers.joinToString(",").ifEmpty { null },
  )

  person.additionalDetails = additionalDetails?.toEntity(person)

  return person
}
