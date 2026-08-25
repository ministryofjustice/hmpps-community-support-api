package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Person
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Referral
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

data class CheckDraftReferralDetailsBffResponseDto(
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
    fun from(referral: Referral, person: Person): CheckDraftReferralDetailsBffResponseDto = CheckDraftReferralDetailsBffResponseDto(
      id = referral.id,
      referenceNumber = referral.referenceNumber,
      createdDate = referral.createdAt,
      personDetailsTableData = PersonDetailsTableDataDto.from(person),
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
    val name: RefereeNameDto,
    val crn: String,
    val dateOfBirth: String,
    val preferredLanguage: String,
    val disabilities: String,
    val prisonNumbers: String?,
    val currentCircumstances: String,
  ) {
    companion object {
      fun from(person: Person): PersonDetailsTableDataDto = PersonDetailsTableDataDto(
        name = RefereeNameDto(firstName = person.firstName, lastName = person.lastName),
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
        sex = person.gender,
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

  data class RiskInformationDetailsTableDataDto(
    val whoIsAtRisk: String? = null,
    val natureOfRisk: String? = null,
    val riskImminence: String? = null,
    val riskOfSelfHarm: String? = null,
    val riskOfSuicide: String? = null,
    val riskToSelfHostelSetting: String? = null,
    val riskToSelfVulnerability: String? = null,
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
    val needsInterpreter: Boolean? = null,
    val interpreterLanguage: String? = null,
  ) {
    companion object {
      fun from(): AdditionalSupportNeedsDetailsTableDataDto = AdditionalSupportNeedsDetailsTableDataDto()
    }
  }

  data class PersonNeedsDetailsTableDataDto(
    val hasAccommodationNeeds: Boolean? = null,
    val accommodationDetails: String? = null,
    val employmentAndEducation: String? = null,
    val financialDetails: String? = null,
    val personalRelationshipsCommunityDetails: String? = null,
    val drugUseDetails: String? = null,
    val alcoholUseDetails: String? = null,
    val healthWellbeingDetails: String? = null,
    val thinkingBehavioursAttitudeDetails: String? = null,
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

  data class AdditionalReferralInformationTableDataDto(
    val serviceCompletionDate: OffsetDateTime? = null,
    val serviceCompletionDateReason: String? = null,
    val serviceDays: Int? = null,
    val offence: String? = null,
    val offenceSubCategory: String? = null,
    val outcome: String? = null,
    val sentenceEndDate: LocalDate? = null,
  ) {
    companion object {
      fun from(referral: Referral): AdditionalReferralInformationTableDataDto = AdditionalReferralInformationTableDataDto(
        serviceCompletionDate = referral.targetServiceCompletionDate,
        serviceCompletionDateReason = referral.targetServiceCompletionDateReason,
        serviceDays = referral.serviceDays,
        offence = null,
        offenceSubCategory = null,
        outcome = null,
        sentenceEndDate = null,
      )
    }
  }

  data class MainPOCDetailsTableDataDto(
    val areTheseDetailsCorrect: Boolean? = null,
    val name: String? = null,
    val jobRole: String? = null,
    val email: String? = null,
    val phoneNumber: String? = null,
    val pdu: String? = null,
    val isProbationOfficer: Boolean? = null,
    val teamPhoneNumber: String? = null,
  ) {
    companion object {
      fun from(): MainPOCDetailsTableDataDto = MainPOCDetailsTableDataDto()
    }
  }
}
