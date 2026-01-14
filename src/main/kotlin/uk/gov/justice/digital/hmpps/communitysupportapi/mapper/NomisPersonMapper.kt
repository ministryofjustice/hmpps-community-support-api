package uk.gov.justice.digital.hmpps.communitysupportapi.mapper

import uk.gov.justice.digital.hmpps.communitysupportapi.dto.nomis.NomisPersonDto
import uk.gov.justice.digital.hmpps.communitysupportapi.model.Person
import uk.gov.justice.digital.hmpps.communitysupportapi.model.PersonAdditionalDetails
import uk.gov.justice.digital.hmpps.communitysupportapi.model.PersonIdentifier

fun NomisPersonDto.toPerson() = Person(
  identifier = PersonIdentifier.PrisonerNumber(prisonerNumber),
  firstName = firstName,
  lastName = lastName,
  dateOfBirth = dateOfBirth,
  sex = gender,
)

fun NomisPersonDto.toAdditionalDetails() = PersonAdditionalDetails(
  ethnicity = ethnicity,
  preferredLanguage = languages.firstOrNull()?.code,
  neurodiverseConditions = null,
  religionOrBelief = religion,
  transgender = null,
  sexualOrientation = null,
  address = addresses.firstOrNull()?.fullAddress,
  phoneNumber = phoneNumbers.firstOrNull()?.number,
  emailAddress = emailAddresses.firstOrNull()?.email,
)
