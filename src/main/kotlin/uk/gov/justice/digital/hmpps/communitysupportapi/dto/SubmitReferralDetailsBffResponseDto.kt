package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Person
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Referral
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralUserAssignment
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.RiskInformation
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.PersonAdditionalSupportNeeds
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralCriminogenicNeeds
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ContractArea
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralOffenceSentence
import java.time.OffsetDateTime
import java.time.LocalDate
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
    fun from(referral: Referral, person: Person,): SubmitDetailsBffResponseDto = SubmitDetailsBffResponseDto(
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
        sex = person.gender, // Double check
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
      fun from(riskInformation: RiskInformation): RiskInformationDetailsTableDataDto = RiskInformationDetailsTableDataDto(
        whoIsAtRisk = riskInformation.riskSummaryWhoIsAtRisk,
        natureOfRisk = riskInformation.riskSummaryNatureOfRisk,
        riskImminence = riskInformation.riskSummaryRiskImminence,
        riskOfSelfHarm = riskInformation.riskToSelfHarm,
        riskOfSuicide = riskInformation.riskToSelfSuicide,
        riskToSelfHostelSetting = riskInformation.riskToSelfHostelSetting,
        riskToSelfVulnerability = riskInformation.riskToSelfVulnerability,
        additionalInformation = riskInformation.additionalInformation,
      )
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
      fun from(additionalSupportNeed: PersonAdditionalSupportNeeds): AdditionalSupportNeedsDetailsTableDataDto = AdditionalSupportNeedsDetailsTableDataDto(
        physicalHealth = additionalSupportNeed.physicalHealthDetails,
        mentalOrEmotionalHealth = additionalSupportNeed.mentalEmotionalHealthDetails,
        neurodiversity = additionalSupportNeed.neurodiversityDetails,
        locationAndTravel = additionalSupportNeed.locationTravelDetails,
        caringResponsibilities = additionalSupportNeed.caringResponsibilitiesDetails,
        employmentResponsibilities = additionalSupportNeed.employmentResponsibilitiesDetails,
        diversity = additionalSupportNeed.diversityDetails,
        anyOtherNeeds = additionalSupportNeed.anythingElseDetails,
        needsInterpreter = additionalSupportNeed.interpreterNeeded,
        interpreterLanguage = additionalSupportNeed.interpreterLanguage,
      )
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
    val thinkingBehaviourAndAttitudes: String? = null,
  ) {
    companion object {
      fun from(crimogenicNeeds: ReferralCriminogenicNeeds): PersonNeedsDetailsTableDataDto = PersonNeedsDetailsTableDataDto(
        hasAccommodationNeeds = crimogenicNeeds.hasAccommodationNeeds,
        accommodationDetails = crimogenicNeeds.accommodationDetails,
        employmentAndEducation = crimogenicNeeds.employmentEducationDetails,
        financialDetails = crimogenicNeeds.financialDetails,
        personalRelationshipsCommunityDetails = crimogenicNeeds.personalRelationshipsCommunityDetails,
        drugUseDetails = crimogenicNeeds.drugUseDetails,
        alcoholUseDetails = crimogenicNeeds.alcoholUseDetails,
        healthWellbeingDetails = crimogenicNeeds.healthWellbeingDetails,
        // thinkingBehaviourAndAttitudes Not present in ReferralCriminogenicNeeds look up
      )
    }
  }

  data class ReferralAreaTableDataDto(
    val area: String? = null,
  ) {
    companion object {
      fun from(contractArea: ContractArea): ReferralAreaTableDataDto = ReferralAreaTableDataDto(
        area = contractArea.area,
      )
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
      fun from(referral: Referral, offenceSentence: ReferralOffenceSentence): AdditionalReferralInformationTableDataDto = AdditionalReferralInformationTableDataDto(
        serviceCompletionDate = referral.targetServiceCompletionDate,
        serviceCompletionDateReason = referral.targetServiceCompletionDateReason,
        serviceDays = referral.serviceDays,
        offence = offenceSentence.offence,
        offenceSubCategory = offenceSentence.offenceSubCategory,
        outcome = offenceSentence.outcome,
        sentenceEndDate = offenceSentence.sentenceEndDate,
      )
    }
  }

  data class MainPOCDetailsTableDataDto(val detailsFromNDelius: String? = null) {
    companion object {
      fun from(): MainPOCDetailsTableDataDto = MainPOCDetailsTableDataDto()
    }
  }

  
}
