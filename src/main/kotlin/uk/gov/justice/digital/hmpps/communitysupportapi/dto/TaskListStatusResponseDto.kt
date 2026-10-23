package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import uk.gov.justice.digital.hmpps.communitysupportapi.dto.delius.CommunityManagerDto
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Person
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.PersonAdditionalSupportNeeds
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ProbationPractitionerDetails
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferenceDataCommunityServiceProvider
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Referral
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralCriminogenicNeeds
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.RiskInformation

data class TaskListStatusResponseDto(
  val fullName: String,
  val confirmPersonalDetailsStatus: TaskListStatusItem,
  val checkRiskInformationStatus: TaskListStatusItem,
  val selectThePersonsNeedsStatus: TaskListStatusItem,
  val addDetailsOfAnyAdditionalSupportNeedsStatus: TaskListStatusItem,
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
    ): TaskListStatusResponseDto {
      val fullName = person.firstName + " " + person.lastName
      val confirmPersonalDetailsStatus = TaskListStatusItem.completed()
      val checkRiskInformationStatus = getRiskInfoStatus(riskInfo)
      val selectThePersonsNeedsStatus = getCriminogenicNeedsStatus(criminogenicNeeds)
      val addDetailsOfAnyAdditionalSupportNeedsStatus = getAdditionalSupportNeedsStatus(additionalSupportNeeds)
      val addAdditionalInformationStatus = getAdditionalInformationStatus(referral)
      val selectAnAreaForReferralStatus = getCommunityServiceProviderStatus(referenceDataCommunityServiceProvider)
      val contactDetailsStatus = getContactDetailsStatus(communityManager, probationPractitionerDetails)
      val canStartCheckAnswers = listOf(
        confirmPersonalDetailsStatus,
        checkRiskInformationStatus,
        selectThePersonsNeedsStatus,
        addDetailsOfAnyAdditionalSupportNeedsStatus,
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
        fullName,
        confirmPersonalDetailsStatus,
        checkRiskInformationStatus,
        selectThePersonsNeedsStatus,
        addDetailsOfAnyAdditionalSupportNeedsStatus,
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

    private fun getAdditionalInformationStatus(referral: Referral): TaskListStatusItem {
      val hasTargetServiceCompletionDate = referral.targetServiceCompletionDate != null
      val hasTargetServiceCompletionDateReason = !referral.targetServiceCompletionDateReason.isNullOrBlank()
      val hasServiceDays = referral.serviceDays != null

      val populatedFieldCount = listOf(
        hasTargetServiceCompletionDate,
        hasTargetServiceCompletionDateReason,
        hasServiceDays,
      ).count { it }

      return when (populatedFieldCount) {
        3 -> TaskListStatusItem.completed()
        0 -> TaskListStatusItem.notStarted()
        else -> TaskListStatusItem.notStarted()
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
