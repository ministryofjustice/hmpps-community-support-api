package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Person
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Referral
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralUserAssignment
import uk.gov.justice.digital.hmpps.communitysupportapi.mapper.toCaseWorkerDto
import java.time.OffsetDateTime
import java.util.UUID

data class SubmitDetailsBffResponseDto(
  val id: UUID,
  val referenceNumber: String?,
  val createdDate: OffsetDateTime,
  val personDetailsTableData: PersonDetailsTableDataDto,
  val equalityDetailsTableData: EqualityDetailsTableDataDto,
  val additionalInformationDetailsTableData: AdditionalInformationDetailsTableDataDto,
  val contactDetailsTableData: ContactDetailsTableDataDto,
  val riskInformationDetailsTableData: RiskInformationDetailsTableDataDto,
  val additionalSupportNeedsDetailsTableData: AdditionalSupportNeedsDetailsTableDataDto,
  val personNeedsDetailsTableData: PersonNeedsDetailsTableDataDto,
  val referralAreaTableData: ReferralAreaTableDataDto,
  val mainPocDetailsTableData: MainPOCDetailsTableDataDto,
) {
  companion object {
    fun from(referral: Referral, person: Person, referralAssignments: List<ReferralUserAssignment>): SubmitDetailsBffResponseDto = SubmitDetailsBffResponseDto(
      id = referral.id,
      referenceNumber = referral.referenceNumber,
      createdDate = referral.createdAt,
      personDetailsTableData = PersonDetailsTableDataDto.from(person, referral),
      equalityDetailsTableData = EqualityDetailsTableDataDto.from(person),
      contactDetailsTableData = ContactDetailsTableDataDto.from(person),
      additionalInformationDetailsTableData = AdditionalInformationDetailsTableDataDto.from(),
      riskInformationDetailsTableData = RiskInformationDetailsTableDataDto.from(),
      additionalSupportNeedsDetailsTableData = AdditionalSupportNeedsDetailsTableDataDto.from(),
      personNeedsDetailsTableData = PersonNeedsDetailsTableDataDto.from(),
      referralAreaTableData = ReferralAreaTableDataDto.from(),
      mainPocDetailsTableData = MainPOCDetailsTableDataDto.from(),
    )
  }

  data class PersonDetailsTableDataDto(
    val name: String,
    val crn: String,
    val dateOfBirth: String,
    val preferredLanguage: String,
    val disabilities: String,
    val prisonNumbers: String?,
    val currentCircumstances: String,
  ) {
    companion object {
      fun from(person: Person, referral: Referral): PersonDetailsTableDataDto = PersonDetailsTableDataDto(
        name = "${person.firstName} ${person.lastName}",
        // TODO: Why is this coming from the Referral, not from the person?  --TWC 2026-07-13
        crn = person.identifier,
        dateOfBirth = person.dateOfBirth.toString(),
        preferredLanguage = person.additionalDetails?.preferredLanguage ?: "",
        disabilities = "", // OffenderProfileDto
        prisonNumbers = person.prisonNumbers,
        currentCircumstances = "", // Delius OffenderProfileDto
      )
    }
  }

  data class EqualityDetailsTableDataDto(
    val ethnicity: String?,
    val religionOrBelief: String?,
    val sex: String,
  ) {
    companion object {
      fun from(person: Person): EqualityDetailsTableDataDto = EqualityDetailsTableDataDto(
        ethnicity = person.additionalDetails?.ethnicity ?: "",
        religionOrBelief = person.additionalDetails?.religionOrBelief ?: "",
        sex = person.sex,
      )
    }
  }

  data class AdditionalInformationDetailsTableDataDto(
    val homeOfficeInterest: String? = null,
    val offenderPersonalityDisorderPathway: String? = null,
  ) {
    companion object {
      fun from(): AdditionalInformationDetailsTableDataDto = AdditionalInformationDetailsTableDataDto()
    }
  }

  data class RiskInformationDetailsTableDataDto(
    val whoIsAtRisk: String? = null,
    val natureOfRisk: String? = null,
    val circumstancesOfLikelyOffending: String? = null,
    val riskOfSelfHarm: String? = null,
    val riskOfSuicide: String? = null,
    val concernsCopingInApprovedPremisesOrHostel: String? = null,
    val concernsInRelationToVulnerability: String? = null,
    val additionalInformation: String? = null,
  ) {
    companion object {
      fun from(): RiskInformationDetailsTableDataDto = RiskInformationDetailsTableDataDto()
    }
  }

  data class AdditionalSupportNeedsDetailsTableDataDto(
    val physicalHealth: String? = null,
    val mentalOrEmotionalHealth: String? = null,
    val neurodiversity: String? = null,
    val locationAndTravel: String? = null,
    val caringResponsibilities: String? = null,
    val employmentResponsibilities: String? = null,
    val diversity: String? = null,
    val anyOtherNeeds: String? = null,
    val needsInterpreter: String? = null,
    val interpreterLanguage: String? = null,
  ) {
    companion object {
      fun from(): AdditionalSupportNeedsDetailsTableDataDto = AdditionalSupportNeedsDetailsTableDataDto()
    }
  }

  data class PersonNeedsDetailsTableDataDto(
    val accommodation: String? = null,
    val employmentAndEducation: String? = null,
    val finances: String? = null,
    val personalRelationshipsAndCommunity: String? = null,
    val drugUse: String? = null,
    val alcoholUse: String? = null,
    val healthAndWellbeing: String? = null,
    val thinkingBehaviourAndAttitudes: String? = null,
  ) {
    companion object {
      fun from(): PersonNeedsDetailsTableDataDto = PersonNeedsDetailsTableDataDto()
    }
  }

  data class ReferralAreaTableDataDto(
    val area: String? = null,
  ) {
    companion object {
      fun from(): ReferralAreaTableDataDto = ReferralAreaTableDataDto()
    }
  }

  data class MainPOCDetailsTableDataDto(
    val serviceCompletionDate: String? = null,
    val serviceCompletionDateReason: String? = null,
    val serviceDays: String? = null,
    val offence: String? = null,
    val offenceSubcategory: String? = null,
    val outcome: String? = null,
    val sentenceEndDate: String? = null,
  ) {
    companion object {
      fun from(): MainPOCDetailsTableDataDto = MainPOCDetailsTableDataDto()
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
