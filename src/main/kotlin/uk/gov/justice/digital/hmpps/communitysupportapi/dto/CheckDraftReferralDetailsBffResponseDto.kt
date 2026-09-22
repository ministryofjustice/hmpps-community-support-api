package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import uk.gov.justice.digital.hmpps.communitysupportapi.dto.arns.ArnsRiskDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.arns.CommunitySupportRiskDto
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Person
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Referral
import uk.gov.justice.digital.hmpps.communitysupportapi.model.Disability
import uk.gov.justice.digital.hmpps.communitysupportapi.model.PersonDetailsAndCircumstances
import uk.gov.justice.digital.hmpps.communitysupportapi.model.PersonIdentifier
import uk.gov.justice.digital.hmpps.communitysupportapi.model.PersonalCircumstance
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

private fun riskValue(risk: ArnsRiskDto?): String? = if (risk?.currentConcernsReason.isNullOrBlank()) {
  when (risk?.riskIndicator) {
    "YES" -> "Yes"
    "NO" -> "No"
    "DK" -> "Don't know"
    else -> null
  }
} else {
  risk.currentConcernsReason
}

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
      personIdentifier: PersonIdentifier,
      personalDetailsAndCircumstances: PersonDetailsAndCircumstances,
      communitySupportRiskDto: CommunitySupportRiskDto,
      nationalities: List<String>,
    ): CheckDraftReferralDetailsBffResponseDto = CheckDraftReferralDetailsBffResponseDto(
      id = referral.id,
      referenceNumber = referral.referenceNumber,
      createdDate = referral.createdAt,
      personDetailsTableData = DraftPersonDetailsTableDataDto.from(person, personIdentifier, personalDetailsAndCircumstances),
      equalityDetailsTableData = DraftEqualityDetailsTableDataDto.from(person, nationalities),
      contactDetailsTableData = DraftContactDetailsTableDataDto.from(person),
      additionalInformationDetailsTableData = DraftAdditionalInformationDetailsTableDataDto.from(personalDetailsAndCircumstances),
      riskInformationDetailsTableData = DraftRiskInformationDetailsTableDataDto.from(communitySupportRiskDto),
      additionalSupportNeedsDetailsTableData = DraftAdditionalSupportNeedsDetailsTableDataDto.from(),
      personNeedsDetailsTableData = DraftPersonNeedsDetailsTableDataDto.from(),
      referralAreaTableData = DraftReferralAreaTableDataDto.from(),
      mainPocDetailsTableData = DraftMainPOCDetailsTableDataDto.from(),
    )
  }

  data class DraftPersonDetailsTableDataDto(
    val name: RefereeNameDto,
    val crn: String?,
    val prisonNumber: String?,
    val dateOfBirth: LocalDate,
    val preferredLanguage: String,
    val personalCircumstances: List<PersonalCircumstance> = emptyList(),
    val disabilities: List<Disability> = emptyList(),
  ) {
    companion object {
      fun from(
        person: Person,
        personIdentifier: PersonIdentifier,
        personalDetailsAndCircumstances: PersonDetailsAndCircumstances,
      ): DraftPersonDetailsTableDataDto = DraftPersonDetailsTableDataDto(
        name = RefereeNameDto(firstName = person.firstName, lastName = person.lastName),
        crn = if (personIdentifier is PersonIdentifier.Crn) personIdentifier.value else null,
        prisonNumber = if (personIdentifier is PersonIdentifier.PrisonerNumber) personIdentifier.value else null,
        dateOfBirth = person.dateOfBirth,
        preferredLanguage = person.additionalDetails?.preferredLanguage ?: "",
        personalCircumstances = personalDetailsAndCircumstances.personalCircumstances,
        disabilities = personalDetailsAndCircumstances.disabilities,
      )
    }
  }

  data class DraftEqualityDetailsTableDataDto(
    val nationality: String?,
    val ethnicity: String?,
    val religionOrBelief: String?,
    val sex: String,
  ) {
    companion object {
      fun from(person: Person, nationalities: List<String> = emptyList()): DraftEqualityDetailsTableDataDto = DraftEqualityDetailsTableDataDto(
        ethnicity = person.additionalDetails?.ethnicity ?: "",
        religionOrBelief = person.additionalDetails?.religionOrBelief ?: "",
        sex = person.gender,
        nationality = nationalities.joinToString(", "),
      )
    }
  }

  data class DraftAdditionalInformationDetailsTableDataDto(
    val ofHomeOfficeInterest: Boolean? = null,
    val homeOfficeInterestNotes: String? = null,
    val offenderPersonalityDisorderPathway: String? = null,
  ) {
    companion object {
      fun from(personalDetailsAndCircumstances: PersonDetailsAndCircumstances): DraftAdditionalInformationDetailsTableDataDto = DraftAdditionalInformationDetailsTableDataDto(
        ofHomeOfficeInterest = personalDetailsAndCircumstances.ofHomeOfficeInterest,
        homeOfficeInterestNotes = personalDetailsAndCircumstances.homeOfficeInterestNotes,
        offenderPersonalityDisorderPathway = personalDetailsAndCircumstances.offenderPersonalityDisorder,
      )
    }
  }

  data class DraftContactDetailsTableDataDto(
    val phoneNumber: String?,
    val mobileNumber: String?,
    val email: String?,
    val address: String?,
    val addressUpdatedAt: OffsetDateTime?,
    val noFixedAddress: Boolean?,
    val inCustody: Boolean,
    val addressType: String?,
    val addressStartDate: LocalDate?,
    val addressNotes: String?,
  ) {
    companion object {
      fun from(person: Person): DraftContactDetailsTableDataDto = DraftContactDetailsTableDataDto(
        phoneNumber = person.additionalDetails?.phoneNumber,
        mobileNumber = null,
        email = person.additionalDetails?.emailAddress,
        address = person.additionalDetails?.address,
        addressUpdatedAt = null,
        noFixedAddress = null,
        inCustody = false,
        addressType = null,
        addressStartDate = null,
        addressNotes = null,
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
      fun from(riskInformation: CommunitySupportRiskDto): DraftRiskInformationDetailsTableDataDto {
        val summary = riskInformation.summary
        val riskToSelf = riskInformation.riskToSelf
        return DraftRiskInformationDetailsTableDataDto(
          whoIsAtRisk = summary?.whoIsAtRisk,
          natureOfRisk = summary?.natureOfRisk,
          riskImminence = summary?.riskImminence,
          riskOfSelfHarm = riskValue(riskToSelf?.selfHarm),
          riskOfSuicide = riskValue(riskToSelf?.suicide),
          riskToSelfHostelSetting = riskValue(riskToSelf?.hostelSetting),
          riskToSelfVulnerability = riskValue(riskToSelf?.vulnerability),
          additionalInformation = riskInformation.additionalInformation,
        )
      }
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
