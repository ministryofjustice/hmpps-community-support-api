package uk.gov.justice.digital.hmpps.communitysupportapi.mapper

import uk.gov.justice.digital.hmpps.communitysupportapi.dto.delius.AddressDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.delius.DeliusPersonDto
import uk.gov.justice.digital.hmpps.communitysupportapi.exception.NotFoundException
import uk.gov.justice.digital.hmpps.communitysupportapi.model.Person
import uk.gov.justice.digital.hmpps.communitysupportapi.model.PersonAdditionalDetails
import uk.gov.justice.digital.hmpps.communitysupportapi.model.PersonIdentifier

fun DeliusPersonDto.toPerson() = Person(
  identifier = otherIds?.crn?.let { PersonIdentifier.Crn(it) } ?: throw NotFoundException("CRN is missing"),
  firstName = firstName,
  lastName = surname,
  dateOfBirth = dateOfBirth,
  sex = gender,
)

fun DeliusPersonDto.toAdditionalDetails() = PersonAdditionalDetails(
  ethnicity = offenderProfile?.ethnicity,
  preferredLanguage = offenderProfile?.offenderLanguages?.primaryLanguage,
  neurodiverseConditions = null,
  religionOrBelief = offenderProfile?.religion,
  transgender = null,
  sexualOrientation = offenderProfile?.sexualOrientation,
  address = contactDetails.addresses.firstOrNull()?.toDisplayString(),
  phoneNumber = contactDetails.phoneNumbers.firstOrNull()?.number,
  emailAddress = contactDetails.emailAddresses.firstOrNull(),
)

fun AddressDto.toDisplayString(): String = listOfNotNull(
  addressNumber,
  buildingName,
  streetName,
  district,
  town,
  county,
  postcode,
)
  .joinToString(", ")
