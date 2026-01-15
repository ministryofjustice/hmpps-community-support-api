package uk.gov.justice.digital.hmpps.communitysupportapi.mapper

import uk.gov.justice.digital.hmpps.communitysupportapi.dto.delius.DeliusPersonDto
import uk.gov.justice.digital.hmpps.communitysupportapi.model.Person
import uk.gov.justice.digital.hmpps.communitysupportapi.model.PersonAdditionalDetails
import uk.gov.justice.digital.hmpps.communitysupportapi.model.PersonIdentifier

fun DeliusPersonDto.toPerson() = Person(
  identifier = PersonIdentifier.Crn(otherIds?.crn!!),
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
  address = contactDetails.addresses.firstOrNull()?.id.toString(),
  phoneNumber = contactDetails.phoneNumbers.firstOrNull()?.number,
  emailAddress = contactDetails.emailAddresses.firstOrNull(),
)
