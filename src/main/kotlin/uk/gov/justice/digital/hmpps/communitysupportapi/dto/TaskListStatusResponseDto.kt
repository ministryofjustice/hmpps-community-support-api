package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import uk.gov.justice.digital.hmpps.communitysupportapi.dto.delius.CommunityManagerDto
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Person
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.PersonAdditionalSupportNeeds
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ProbationPractitionerDetails
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferenceDataCommunityServiceProvider
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralOffenceSentence
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Referral
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralCriminogenicNeeds
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.RiskInformation

data class TaskListStatusResponseDto(
  val refereeName: RefereeNameDto,
  val confirmPersonalDetailsStatus: TaskListStatusItem,
  val checkRiskInformationStatus: TaskListStatusItem,
  val selectThePersonsNeedsStatus: TaskListStatusItem,
  val addAdditionalSupportNeedsStatus: TaskListStatusItem,
  val addAdditionalInformationStatus: TaskListStatusItem,
  val selectAnAreaForReferralStatus: TaskListStatusItem,
  val contactDetailsStatus: ContactDetailsDto,
  val checkYourAnswersStatus: TaskListStatusItem,
) {
  companion object {
    fun from(
      referral: Referral,
      person: Person,
      additionalSupportNeeds: PersonAdditionalSupportNeeds?,
      riskInfo: RiskInformation?,
      criminogenicNeeds: ReferralCriminogenicNeeds?,
      referenceDataCommunityServiceProvider: ReferenceDataCommunityServiceProvider?,
      communityManager: CommunityManagerDto? = null,
      probationPractitionerDetails: ProbationPractitionerDetails? = null,
      offenceSentence: ReferralOffenceSentence? = null,
    ): TaskListStatusResponseDto {
      val confirmPersonalDetailsStatus = TaskListStatusItem.completed()
      val checkRiskInformationStatus = getRiskInfoStatus(riskInfo)
      val selectThePersonsNeedsStatus = getCriminogenicNeedsStatus(criminogenicNeeds)
      val addAdditionalSupportNeedsStatus = getAdditionalSupportNeedsStatus(additionalSupportNeeds)
      val addAdditionalInformationStatus = getAdditionalInformationStatus(referral, offenceSentence)
      val selectAnAreaForReferralStatus = getCommunityServiceProviderStatus(referenceDataCommunityServiceProvider)
      val contactDetailsStatus = getContactDetailsStatus(communityManager, probationPractitionerDetails)
      val canStartCheckAnswers = listOf(
        confirmPersonalDetailsStatus,
        checkRiskInformationStatus,
        selectThePersonsNeedsStatus,
        addAdditionalSupportNeedsStatus,
        addAdditionalInformationStatus,
        selectAnAreaForReferralStatus,
        contactDetailsStatus.status,
      )
        .all { it.completed }

      val checkYourAnswersStatus = if (canStartCheckAnswers) {
        TaskListStatusItem.notStarted()
      } else {
        TaskListStatusItem.cannotStart()
      }

      return TaskListStatusResponseDto(
        RefereeNameDto(firstName = person.firstName, lastName = person.lastName),
        confirmPersonalDetailsStatus,
        checkRiskInformationStatus,
        selectThePersonsNeedsStatus,
        addAdditionalSupportNeedsStatus,
        addAdditionalInformationStatus,
        selectAnAreaForReferralStatus,
        contactDetailsStatus,
        checkYourAnswersStatus,
      )
    }

    private fun getCommunityServiceProviderStatus(referenceDataCommunityServiceProvider: ReferenceDataCommunityServiceProvider?): TaskListStatusItem = referenceDataCommunityServiceProvider?.let { TaskListStatusItem.completed() } ?: TaskListStatusItem.notStarted()

    private fun getCriminogenicNeedsStatus(criminogenicNeeds: ReferralCriminogenicNeeds?): TaskListStatusItem = criminogenicNeeds?.let { TaskListStatusItem.completed() } ?: TaskListStatusItem.notStarted()

    private fun getAdditionalSupportNeedsStatus(additionalSupportNeeds: PersonAdditionalSupportNeeds?): TaskListStatusItem = if (additionalSupportNeeds?.additionalSupportNeeded != null && additionalSupportNeeds?.interpreterNeeded != null) {
      TaskListStatusItem.completed()
    } else {
      TaskListStatusItem.notStarted()
    }

    private fun getRiskInfoStatus(riskInfo: RiskInformation?): TaskListStatusItem = riskInfo?.let { return TaskListStatusItem.completed() } ?: TaskListStatusItem.notStarted()

    private fun getAdditionalInformationStatus(referral: Referral, offenceSentence: ReferralOffenceSentence?): TaskListStatusItem {
      val hasTargetServiceCompletionDate = referral.targetServiceCompletionDate != null
      val hasTargetServiceCompletionDateReason = !referral.targetServiceCompletionDateReason.isNullOrBlank()
      val hasServiceDays = referral.serviceDays != null
      val hasAdditionalInformationForTheDeliveryPartner = referral.hasAdditionalInformationForTheDeliveryPartner != null
      val hasLicenceConditionsOrZones = offenceSentence?.hasLicenceConditionsOrZones != null

      val populatedFieldCount = listOf(
        hasTargetServiceCompletionDate,
        hasTargetServiceCompletionDateReason,
        hasServiceDays,
        hasAdditionalInformationForTheDeliveryPartner,
        hasLicenceConditionsOrZones,
      ).count { it == true }

      return if (populatedFieldCount == 5) {
        TaskListStatusItem.completed()
      } else {
        TaskListStatusItem.notStarted()
      }
    }

    private fun getContactDetailsStatus(
      communityManagerDto: CommunityManagerDto?,
      savedProbationPractitionerDetails: ProbationPractitionerDetails?,
    ): ContactDetailsDto = when {
      communityManagerDto == null -> ContactDetailsDto(false, TaskListStatusItem.notStarted())
      savedProbationPractitionerDetails == null -> ContactDetailsDto(!isUnallocated(communityManagerDto), TaskListStatusItem.notStarted())
      isUnallocated(communityManagerDto) -> ContactDetailsDto(false, TaskListStatusItem.completed())
      else -> ContactDetailsDto(savedProbationPractitionerDetails.ppDetailsFoundAndCorrect != false, TaskListStatusItem.completed())
    }

    private fun isUnallocated(communityManagerDto: CommunityManagerDto): Boolean {
      val name = communityManagerDto.communityManager?.name ?: return false
      return listOfNotNull(name.forename, name.middleName, name.surname)
        .any { it.contains("unallocated", ignoreCase = true) }
    }
  }
}

data class TaskListStatusItem(
  val completed: Boolean,
  val statusText: String,
  val tag: String? = null,
) {
  companion object {
    fun completed() = TaskListStatusItem(true, "Completed", "govuk-tag--green")
    fun notStarted() = TaskListStatusItem(false, "Not started", "govuk-tag--grey")
    fun cannotStart() = TaskListStatusItem(false, "Cannot Start Yet", null)
  }
}

data class ContactDetailsDto(val isProbationPractitioner: Boolean, val status: TaskListStatusItem)
