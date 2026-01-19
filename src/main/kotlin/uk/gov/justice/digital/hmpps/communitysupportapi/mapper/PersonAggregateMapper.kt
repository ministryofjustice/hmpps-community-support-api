package uk.gov.justice.digital.hmpps.communitysupportapi.mapper

import uk.gov.justice.digital.hmpps.communitysupportapi.dto.PersonDto
import uk.gov.justice.digital.hmpps.communitysupportapi.model.PersonAdditionalDetails
import uk.gov.justice.digital.hmpps.communitysupportapi.model.PersonAggregate
import uk.gov.justice.digital.hmpps.communitysupportapi.model.PersonIdentifier
import java.time.OffsetDateTime
import java.util.UUID
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Person as PersonEntity
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.PersonAdditionalDetails as PersonAdditionalDetailsEntity

fun PersonAggregate.toEntity(): PersonEntity {
  val person = PersonEntity(
    id = UUID.randomUUID(),
    identifier = when (person.identifier) {
      is PersonIdentifier.Crn -> person.identifier.value
      is PersonIdentifier.PrisonerNumber -> person.identifier.value
    },
    firstName = person.firstName,
    lastName = person.lastName,
    dateOfBirth = person.dateOfBirth,
    gender = person.sex,
    createdAt = OffsetDateTime.now(),
    updatedAt = OffsetDateTime.now(),
  )

  person.additionalDetails = additionalDetails?.toEntity(person)

  return person
}

fun PersonAdditionalDetails.toEntity(person: PersonEntity): PersonAdditionalDetailsEntity = PersonAdditionalDetailsEntity(
  id = UUID.randomUUID(),
  person = person,
  ethnicity = ethnicity,
  preferredLanguage = preferredLanguage,
  neurodiverseConditions = neurodiverseConditions,
  religionOrBelief = religionOrBelief,
  transgender = transgender,
  sexualOrientation = sexualOrientation,
  address = address,
  phoneNumber = phoneNumber,
  emailAddress = emailAddress,
)

fun PersonAggregate.toPersonDto(personId: UUID): PersonDto = PersonDto(
  id = personId,
  personIdentifier = when (person.identifier) {
    is PersonIdentifier.Crn -> person.identifier.value
    is PersonIdentifier.PrisonerNumber -> person.identifier.value
  },
  firstName = person.firstName,
  lastName = person.lastName,
  dateOfBirth = person.dateOfBirth,
  sex = person.sex,
  additionalDetails = additionalDetails,
)
