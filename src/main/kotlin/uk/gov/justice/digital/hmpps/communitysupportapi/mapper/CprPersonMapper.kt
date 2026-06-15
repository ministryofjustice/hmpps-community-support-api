package uk.gov.justice.digital.hmpps.communitysupportapi.mapper

import uk.gov.justice.digital.hmpps.communitysupportapi.dto.cpr.CprAddressDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.cpr.CprContactDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.cpr.CprPersonDto
import uk.gov.justice.digital.hmpps.communitysupportapi.exception.NotFoundException
import uk.gov.justice.digital.hmpps.communitysupportapi.model.Person
import uk.gov.justice.digital.hmpps.communitysupportapi.model.PersonAdditionalDetails
import uk.gov.justice.digital.hmpps.communitysupportapi.model.PersonIdentifier
import java.time.LocalDate

fun CprPersonDto.toProbationPerson(): Person = Person(
  identifier = PersonIdentifier.Crn(
    identifiers.crns.firstOrNull()
      ?: throw NotFoundException("CRN not found in Core Person Record response"),
  ),
  firstName = firstName ?: throw NotFoundException("First name missing in Core Person Record response"),
  lastName = lastName ?: throw NotFoundException("Last name missing in Core Person Record response"),
  dateOfBirth = dateOfBirth?.let { LocalDate.parse(it) }
    ?: throw NotFoundException("Date of birth missing in Core Person Record response"),
  sex = sex?.description ?: "Unknown",
  title = title?.description,
  middleNames = middleNames,
)

fun CprPersonDto.toPrisonPerson(): Person = Person(
  identifier = PersonIdentifier.PrisonerNumber(
    identifiers.prisonNumbers.firstOrNull()
      ?: throw NotFoundException("Prison number not found in Core Person Record response"),
  ),
  firstName = firstName ?: throw NotFoundException("First name missing in Core Person Record response"),
  lastName = lastName ?: throw NotFoundException("Last name missing in Core Person Record response"),
  dateOfBirth = dateOfBirth?.let { LocalDate.parse(it) }
    ?: throw NotFoundException("Date of birth missing in Core Person Record response"),
  sex = sex?.description ?: "Unknown",
  title = title?.description,
  middleNames = middleNames,
)

fun CprPersonDto.toAdditionalDetails(): PersonAdditionalDetails {
  val allContacts = addresses.flatMap { it.contacts }
  val firstAddress = addresses.firstOrNull { it.status?.code == "M" }
  return PersonAdditionalDetails(
    ethnicity = ethnicity?.description,
    preferredLanguage = null,
    neurodiverseConditions = null,
    religionOrBelief = religion?.description,
    transgender = null,
    sexualOrientation = sexualOrientation?.description,
    genderIdentity = sex?.description,
    nationalities = nationalities.mapNotNull { it.description },
    interestToImmigration = interestToImmigration,
    address = firstAddress?.toDisplayString(),
    addressType = firstAddress?.usages?.firstOrNull { it.isActive }?.description,
    addressTypeVerified = firstAddress?.typeVerified == true,
    addressStartDate = firstAddress?.startDate?.let { LocalDate.parse(it) },
    addressNotes = firstAddress?.comment,
    phoneNumber = allContacts.firstPhoneNumber(),
    mobileNumber = allContacts.firstMobileNumber(),
    emailAddress = allContacts.firstEmailAddress(),
    disability = disability,
  )
}

fun CprAddressDto.toDisplayString(): String = listOfNotNull(
  buildingNumber,
  buildingName,
  thoroughfareName,
  dependentLocality,
  postTown,
  county,
  postcode,
).joinToString(", ")

private fun List<CprContactDto>.firstPhoneNumber(): String? = firstOrNull {
  it.value != null &&
    it.type?.code?.contains("HOME", ignoreCase = true) != true
}?.value

private fun List<CprContactDto>.firstMobileNumber(): String? = firstOrNull {
  it.value != null &&
    it.type?.code?.contains("MOBILE", ignoreCase = true) == true
}?.value

private fun List<CprContactDto>.firstEmailAddress(): String? = firstOrNull { it.value?.contains("@") == true }?.value
