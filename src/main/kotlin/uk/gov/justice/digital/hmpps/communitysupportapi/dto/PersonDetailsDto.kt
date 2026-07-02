package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import uk.gov.justice.digital.hmpps.communitysupportapi.model.PersonAggregate
import uk.gov.justice.digital.hmpps.communitysupportapi.model.PersonIdentifier
import java.time.LocalDate
import java.util.UUID

data class WithUpdated<T>(
  val value: T,
  val updated: LocalDate,
)

data class PersonalDetails(
  val personIdentifier: String?,
  val title: String? = null,
  val firstName: String,
  val middleNames: String? = null,
  val lastName: String,
  val dateOfBirth: LocalDate,
  val prisonNumbers: List<String> = emptyList(),
  val preferredLanguage: String? = null,
  val currentCircumstances: WithUpdated<String>,
  val disabilities: WithUpdated<List<String>>,
)

data class EqualityMonitoring(
  val sex: String?,
  val ethnicity: String? = null,
  val neurodiverseConditions: String? = null,
  val religionOrBelief: String? = null,
  val transgender: String? = null,
  val sexualOrientation: String? = null,
  val genderIdentity: String? = null,
  val nationalities: List<String>? = emptyList(),
  val interestToImmigration: Boolean? = null,
  val disability: Boolean? = null,
)

data class Address(
  val address: String? = null,
  val addressType: String? = null,
  val addressTypeVerified: Boolean? = false,
  val addressStartDate: LocalDate? = null,
  val addressNotes: String? = null,
)

data class ContactDetails(
  val phoneNumber: String? = null,
  val mobileNumber: String? = null,
  val emailAddress: String? = null,
  val address: Address,
)

data class PersonDetailsDto(
  val id: UUID,
  val personalDetails: PersonalDetails,
  val equalityMonitoring: EqualityMonitoring,
  val contactDetails: ContactDetails,
) {
  companion object {
    fun from(id: UUID, personAggregate: PersonAggregate): PersonDetailsDto = PersonDetailsDto(
      id = id,
      personalDetails = PersonalDetails(
        personIdentifier = when (personAggregate.person.identifier) {
          is PersonIdentifier.Crn -> personAggregate.person.identifier.value
          is PersonIdentifier.PrisonerNumber -> personAggregate.person.identifier.value
        },
        title = personAggregate.person.title,
        firstName = personAggregate.person.firstName,
        middleNames = personAggregate.person.middleNames,
        lastName = personAggregate.person.lastName,
        dateOfBirth = personAggregate.person.dateOfBirth,
        prisonNumbers = personAggregate.person.prisonNumbers,
        preferredLanguage = personAggregate.additionalDetails?.preferredLanguage,
        // TODO: get this info from nDelius
        currentCircumstances = WithUpdated(
          value = "",
          updated = LocalDate.EPOCH,
        ),
        disabilities = WithUpdated(
          value = emptyList(),
          updated = LocalDate.EPOCH,
        ),
      ),
      equalityMonitoring = EqualityMonitoring(
        sex = personAggregate.person.sex,
        ethnicity = personAggregate.additionalDetails?.ethnicity,
        neurodiverseConditions = personAggregate.additionalDetails?.neurodiverseConditions,
        religionOrBelief = personAggregate.additionalDetails?.religionOrBelief,
        transgender = personAggregate.additionalDetails?.transgender,
        sexualOrientation = personAggregate.additionalDetails?.sexualOrientation,
        genderIdentity = personAggregate.additionalDetails?.genderIdentity,
        nationalities = personAggregate.additionalDetails?.nationalities,
        interestToImmigration = personAggregate.additionalDetails?.interestToImmigration,
        disability = personAggregate.additionalDetails?.disability,
      ),
      contactDetails = ContactDetails(
        phoneNumber = personAggregate.additionalDetails?.phoneNumber,
        mobileNumber = personAggregate.additionalDetails?.mobileNumber,
        emailAddress = personAggregate.additionalDetails?.emailAddress,
        address = Address(
          address = personAggregate.additionalDetails?.address,
          addressType = personAggregate.additionalDetails?.addressType,
          addressTypeVerified = personAggregate.additionalDetails?.addressTypeVerified,
          addressStartDate = personAggregate.additionalDetails?.addressStartDate,
          addressNotes = personAggregate.additionalDetails?.addressNotes,
        ),
      ),
    )
  }
}
