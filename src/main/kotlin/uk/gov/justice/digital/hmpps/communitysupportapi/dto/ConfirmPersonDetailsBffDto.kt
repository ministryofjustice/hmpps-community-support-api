package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import uk.gov.justice.digital.hmpps.communitysupportapi.dto.delius.OffenderProfileDto
import uk.gov.justice.digital.hmpps.communitysupportapi.model.PersonAggregate
import uk.gov.justice.digital.hmpps.communitysupportapi.model.PersonIdentifier
import java.time.LocalDate
import java.util.UUID

data class ConfirmPersonalDetailsCurrentCircumstances(
  /** ISO-formatted Date or empty */
  val updatedAt: String = "",
  val value: String = "",
)

data class ConfirmPersonalDetailsDisabilities(
  /** ISO-formatted Date or empty, representing the most date at which the most recent Disability was created */
  val updatedAt: String = "",
  /** Comma-separated list of disabilities, or "None" if there are none */
  val allDisabilities: String = "",
)

data class ConfirmPersonalPersonalDetails(
  val firstName: String,
  val middleNames: String = "",
  val lastName: String,
  val crn: String = "",
  val prisonNumbers: List<String> = emptyList(), // Can be 0-n
  val dateOfBirth: LocalDate,
  val preferredLanguage: String = "",
  val currentCircumstances: ConfirmPersonalDetailsCurrentCircumstances,
  val disabilities: ConfirmPersonalDetailsDisabilities,
)

data class ConfirmPersonalDetailsEqualityMonitoring(
  val ethnicity: String = "",
  val genderIdentity: String = "",
  val nationalities: List<String> = emptyList(),
  val religionOrBelief: String = "",
  val sex: String = "",
  val sexualOrientation: String = "",
  val transgender: String = "",
)

data class ConfirmPersonDetailsContactAddress(
  val updatedAt: String = "",
  val value: String = "",
  /** This is the address type, e.g. permanent, temporary */
  val type: String = "",
  val startAt: String = "",
  val notes: String = "",
)

data class ConfirmPersonalDetailsContact(
  val phoneNumber: String = "",
  val mobileNumber: String = "",
  val emailAddress: String = "",
  val address: ConfirmPersonDetailsContactAddress = ConfirmPersonDetailsContactAddress(),
)

/**
 * Data that is shown on the /bff/confirm-personal-details/{personReference} page,
 * we have designed it to send back empty strings, as opposed to null values, where
 * values cannot be found in upstream components.
 *
 * This data is similar to the PersonDetailsDto, but is used on a separate screen.
 */
data class ConfirmPersonDetailsBffDto(
  val id: UUID,
  val personalDetails: ConfirmPersonalPersonalDetails,
  val equalityMonitoring: ConfirmPersonalDetailsEqualityMonitoring,
  val contactDetails: ConfirmPersonalDetailsContact,
) {
  companion object {
    fun from(
      id: UUID,
      personAggregate: PersonAggregate,
      offenderProfile: OffenderProfileDto,
    ): ConfirmPersonDetailsBffDto {
      val currentCircumstancesRaw = getCurrentCircumstances(offenderProfile.provisions)
      val disabilitiesRaw = getDisabilities(offenderProfile.disabilities)

      return ConfirmPersonDetailsBffDto(
        id = id,
        personalDetails = ConfirmPersonalPersonalDetails(
          firstName = personAggregate.person.firstName,
          middleNames = personAggregate.person.middleNames ?: "",
          lastName = personAggregate.person.lastName,
          crn = when (val identifier = personAggregate.person.identifier) {
            is PersonIdentifier.Crn -> identifier.value
            else -> ""
          },
          prisonNumbers = personAggregate.person.prisonNumbers,
          dateOfBirth = personAggregate.person.dateOfBirth,
          preferredLanguage = personAggregate.additionalDetails?.preferredLanguage ?: "",
          currentCircumstances = ConfirmPersonalDetailsCurrentCircumstances(
            updatedAt = currentCircumstancesRaw?.updated?.toString() ?: "",
            value = currentCircumstancesRaw?.value ?: "",
          ),
          disabilities = ConfirmPersonalDetailsDisabilities(
            updatedAt = disabilitiesRaw?.updated?.toString() ?: "",
            allDisabilities = disabilitiesRaw?.value?.takeIf { it.isNotEmpty() }?.joinToString(", ") ?: "None",
          ),
        ),

        equalityMonitoring = ConfirmPersonalDetailsEqualityMonitoring(
          sex = personAggregate.person.sex,
          ethnicity = personAggregate.additionalDetails?.ethnicity ?: "",
          religionOrBelief = personAggregate.additionalDetails?.religionOrBelief ?: "",
          transgender = personAggregate.additionalDetails?.transgender ?: "",
          sexualOrientation = personAggregate.additionalDetails?.sexualOrientation ?: "",
          genderIdentity = personAggregate.additionalDetails?.genderIdentity ?: "",
          nationalities = personAggregate.additionalDetails?.nationalities ?: emptyList(),
        ),
        contactDetails = ConfirmPersonalDetailsContact(
          phoneNumber = personAggregate.additionalDetails?.phoneNumber ?: "",
          mobileNumber = personAggregate.additionalDetails?.mobileNumber ?: "",
          emailAddress = personAggregate.additionalDetails?.emailAddress ?: "",
          address = ConfirmPersonDetailsContactAddress(
            value = personAggregate.additionalDetails?.address ?: "",
            type = personAggregate.additionalDetails?.addressType ?: "",
            startAt = personAggregate.additionalDetails?.addressStartDate?.toString() ?: "",
            notes = personAggregate.additionalDetails?.addressNotes ?: "",
          ),
        ),
      )
    }
  }
}
