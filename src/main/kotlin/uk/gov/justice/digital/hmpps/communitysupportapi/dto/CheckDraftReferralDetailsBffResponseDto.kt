package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Person
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Referral
import uk.gov.justice.digital.hmpps.communitysupportapi.model.PersonDetailsAndCircumstances
import uk.gov.justice.digital.hmpps.communitysupportapi.model.PersonalCircumstance
import uk.gov.justice.digital.hmpps.communitysupportapi.model.Disability
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

data class CheckDraftReferralDetailsBffResponseDto(
  val id: UUID,
  val referenceNumber: String?,
  val createdDate: OffsetDateTime,
  val personDetailsTableData: DraftPersonDetailsTableDataDto,
  val equalityDetailsTableData: DraftEqualityDetailsTableDataDto,
  val additionalInformationDetailsTableData: DraftAdditionalInformationDetailsTableDataDto,
  val contactDetailsTableData: DraftContactDetailsTableDataDto,
  val riskInformationDetailsTableData: DraftRiskInformationDetailsTableDataDto,
  val additionalSupportNeedsDetailsTableData: DraftAdditionalSupportNeedsDetailsTableDataDto,
  val personNeedsDetailsTableData: DraftPersonNeedsDetailsTableDataDto,
  val referralAreaTableData: DraftReferralAreaTableDataDto,
  val mainPocDetailsTableData: DraftMainPOCDetailsTableDataDto,
) {
  companion object {
    fun from(
      referral: Referral,
      person: Person,
      personalDetailsAndCircumstances: PersonDetailsAndCircumstances,
    ): CheckDraftReferralDetailsBffResponseDto = CheckDraftReferralDetailsBffResponseDto(
      id = referral.id,
      referenceNumber = referral.referenceNumber,
      createdDate = referral.createdAt,
      personDetailsTableData = DraftPersonDetailsTableDataDto.from(person, personalDetailsAndCircumstances),
      equalityDetailsTableData = DraftEqualityDetailsTableDataDto.from(person),
      contactDetailsTableData = DraftContactDetailsTableDataDto.from(person),
      additionalInformationDetailsTableData = DraftAdditionalInformationDetailsTableDataDto.from(),
      riskInformationDetailsTableData = DraftRiskInformationDetailsTableDataDto.from(),
      additionalSupportNeedsDetailsTableData = DraftAdditionalSupportNeedsDetailsTableDataDto.from(),
      personNeedsDetailsTableData = DraftPersonNeedsDetailsTableDataDto.from(),
      referralAreaTableData = DraftReferralAreaTableDataDto.from(),
      mainPocDetailsTableData = DraftMainPOCDetailsTableDataDto.from(),
    )
  }

  data class DraftPersonDetailsTableDataDto(
    val name: RefereeNameDto,
    val crn: String,
    val dateOfBirth: LocalDate,
    val preferredLanguage: String?,
    val prisonNumbers: String?,
    val personalCircumstances: List<PersonalCircumstance> = emptyList(),
    val disabilities: List<Disability> = emptyList(),
  ) {
    companion object {
      fun from(
        person: Person,
        personalDetailsAndCircumstances: PersonDetailsAndCircumstances,
      ): DraftPersonDetailsTableDataDto {

        return DraftPersonDetailsTableDataDto(
        name = RefereeNameDto(firstName = person.firstName, lastName = person.lastName),
        crn = person.identifier,
        dateOfBirth = person.dateOfBirth,
        preferredLanguage = person.additionalDetails?.preferredLanguage,
        personalCircumstances = personalDetailsAndCircumstances.personalCircumstances,
        disabilities = personalDetailsAndCircumstances.disabilities,
        prisonNumbers = person.prisonNumbers,
      )
      }
    }
  }

  data class DraftEqualityDetailsTableDataDto(
    val ethnicity: String?,
    val religionOrBelief: String?,
    val sex: String,
  ) {
    companion object {
      fun from(person: Person): DraftEqualityDetailsTableDataDto = DraftEqualityDetailsTableDataDto(
        ethnicity = person.additionalDetails?.ethnicity ?: "",
        religionOrBelief = person.additionalDetails?.religionOrBelief ?: "",
        sex = person.gender,
      )
    }
  }

  data class DraftAdditionalInformationDetailsTableDataDto(
    val homeOfficeInterest: String? = null,
    val offenderPersonalityDisorderPathway: String? = null,
  ) {
    companion object {
      fun from(): DraftAdditionalInformationDetailsTableDataDto = DraftAdditionalInformationDetailsTableDataDto()
    }
  }

  data class DraftContactDetailsTableDataDto(
    val phoneNumber: String?,
    val mobileNumber: String?,
    val email: String?,
    val address: String?,
  ) {
    companion object {
      fun from(person: Person): DraftContactDetailsTableDataDto = DraftContactDetailsTableDataDto(
        phoneNumber = person.additionalDetails?.phoneNumber,
        mobileNumber = null,
        email = person.additionalDetails?.emailAddress,
        address = person.additionalDetails?.address,
      )
    }
  }

  data class DraftRiskInformationDetailsTableDataDto(
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
      fun from(): DraftRiskInformationDetailsTableDataDto = DraftRiskInformationDetailsTableDataDto()
    }
  }

  data class DraftAdditionalSupportNeedsDetailsTableDataDto(
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
      fun from(): DraftAdditionalSupportNeedsDetailsTableDataDto = DraftAdditionalSupportNeedsDetailsTableDataDto()
    }
  }

  data class DraftPersonNeedsDetailsTableDataDto(
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
      fun from(): DraftPersonNeedsDetailsTableDataDto = DraftPersonNeedsDetailsTableDataDto()
    }
  }

  data class DraftReferralAreaTableDataDto(
    val area: String? = null,
  ) {
    companion object {
      fun from(): DraftReferralAreaTableDataDto = DraftReferralAreaTableDataDto()
    }
  }

  data class DraftAdditionalReferralInformationTableDataDto(
    val serviceCompletionDate: OffsetDateTime? = null,
    val serviceCompletionDateReason: String? = null,
    val serviceDays: Int? = null,
    val offence: String? = null,
    val offenceSubCategory: String? = null,
    val outcome: String? = null,
    val sentenceEndDate: LocalDate? = null,
  ) {
    companion object {
      fun from(referral: Referral): DraftAdditionalReferralInformationTableDataDto = DraftAdditionalReferralInformationTableDataDto(
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

  data class DraftMainPOCDetailsTableDataDto(
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
      fun from(): DraftMainPOCDetailsTableDataDto = DraftMainPOCDetailsTableDataDto()
    }
  }
}
