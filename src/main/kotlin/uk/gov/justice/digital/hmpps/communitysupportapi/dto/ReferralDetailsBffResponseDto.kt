package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Person
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Referral
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralUserAssignment
import uk.gov.justice.digital.hmpps.communitysupportapi.mapper.toCaseWorkerDto
import java.time.OffsetDateTime
import java.util.UUID

data class ReferralDetailsBffResponseDto(
  val id: UUID,
  val referenceNumber: String?,
  val createdDate: OffsetDateTime,
  val personDetailsTableData: PersonDetailsTableDataDto,
  val equalityDetailsTableData: EqualityDetailsTableDataDto,
  val contactDetailsTableData: ContactDetailsTableDataDto,
  val referralDetailsTableData: ReferralDetailsTableDataDto,
) {
  companion object {
    fun from(referral: Referral, person: Person, referralAssignments: List<ReferralUserAssignment>): ReferralDetailsBffResponseDto = ReferralDetailsBffResponseDto(
      id = referral.id,
      referenceNumber = referral.referenceNumber,
      createdDate = referral.createdAt,
      personDetailsTableData = PersonDetailsTableDataDto.from(person, referral),
      equalityDetailsTableData = EqualityDetailsTableDataDto.from(person),
      contactDetailsTableData = ContactDetailsTableDataDto.from(person),
      referralDetailsTableData = ReferralDetailsTableDataDto.from(referral, referralAssignments),
    )
  }

  data class PersonDetailsTableDataDto(
    val name: String,
    val crn: String,
    val dateOfBirth: String,
    val preferredLanguage: String,
    val disabilities: String,
  ) {
    companion object {
      fun from(person: Person, referral: Referral): PersonDetailsTableDataDto = PersonDetailsTableDataDto(
        name = "${person.firstName} ${person.lastName}",
        crn = referral.crn,
        dateOfBirth = person.dateOfBirth.toString(),
        preferredLanguage = person.additionalDetails?.preferredLanguage ?: "",
        disabilities = "",
      )
    }
  }

  data class EqualityDetailsTableDataDto(
    val ethnicity: String?,
    val religionOrBelief: String?,
    val sex: String,
    val genderIdentity: String,
    val sexualOrientation: String,
    val transgender: String,
  ) {
    companion object {
      fun from(person: Person): EqualityDetailsTableDataDto = EqualityDetailsTableDataDto(
        ethnicity = person.additionalDetails?.ethnicity ?: "",
        religionOrBelief = person.additionalDetails?.religionOrBelief ?: "",
        sex = "",
        genderIdentity = person.gender,
        sexualOrientation = person.additionalDetails?.sexualOrientation ?: "",
        transgender = person.additionalDetails?.transgender ?: "",
      )
    }
  }

  data class ContactDetailsTableDataDto(
    val phoneNumber: String?,
    val mobileNumber: String?,
    val email: String?,
    val address: String?,
  ) {
    companion object {
      fun from(person: Person): ContactDetailsTableDataDto = ContactDetailsTableDataDto(
        phoneNumber = person.additionalDetails?.phoneNumber,
        mobileNumber = null,
        email = person.additionalDetails?.emailAddress,
        address = person.additionalDetails?.address,
      )
    }
  }

  data class ReferralDetailsTableDataDto(
    val referralDate: String,
    val assignedTo: List<CaseWorkerDto>,
  ) {
    companion object {
      fun from(referral: Referral, referralAssignments: List<ReferralUserAssignment>): ReferralDetailsTableDataDto = ReferralDetailsTableDataDto(
        referralDate = referral.createdAt.toString(),
        assignedTo = referralAssignments.map { it.user.toCaseWorkerDto() },
      )
    }
  }
}
