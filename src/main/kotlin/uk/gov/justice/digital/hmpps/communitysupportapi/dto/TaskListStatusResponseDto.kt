package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import uk.gov.justice.digital.hmpps.communitysupportapi.entity.CommunityServiceProvider
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Person
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.PersonAdditionalSupportNeeds
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ProbationPractitionerDetails
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Referral
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralCriminogenicNeeds
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.RiskInformation

data class TaskListStatusResponseDto(
  val fullName: String,
  val confirmPersonalDetailsCompleted: TaskListStatusItem,
  val checkRiskInformationCompleted: TaskListStatusItem,
  val selectThePersonsNeedsCompleted: TaskListStatusItem,
  val addDetailsOfAnyAdditionalSupportNeedsCompleted: TaskListStatusItem,
  val addAdditionalInformationCompleted: TaskListStatusItem,
  val addDetailsOfMainPointOfContactCompleted: TaskListStatusItem,
  val selectAnAreaForReferralCompleted: TaskListStatusItem,
  val checkProbationPractitionerDetailsCompleted: TaskListStatusItem?,
  val addMainPointOfContactCompleted: TaskListStatusItem?,
) {
  companion object {
    fun from(
      referral: Referral,
      person: Person,
      additionalSupportNeeds: PersonAdditionalSupportNeeds?,
      riskInfo: RiskInformation?,
      criminogenicNeeds: ReferralCriminogenicNeeds?,
      communityServiceProvider: CommunityServiceProvider?,
      probationPractitionerDetails: ProbationPractitionerDetailsBffResponseDto? = null,
      savedProbationPractitionerDetails: ProbationPractitionerDetails? = null,
    ) = TaskListStatusResponseDto(
      fullName = person.firstName + " " + person.lastName,
      TaskListStatusItem.notStarted(),
      getRiskInfoStatus(riskInfo),
      getCriminogenicNeedsStatus(criminogenicNeeds),
      getAdditionalSupportNeedsStatus(additionalSupportNeeds),
      getAdditionalInformationStatus(referral),
      TaskListStatusItem.notStarted(),
      getCommunityServiceProviderStatus(communityServiceProvider),
      getCheckProbationPractitionerDetailsStatus(probationPractitionerDetails, savedProbationPractitionerDetails),
      getAddMainPointOfContactStatus(probationPractitionerDetails, savedProbationPractitionerDetails),
    )

    private fun getCommunityServiceProviderStatus(communityServiceProvider: CommunityServiceProvider?): TaskListStatusItem = communityServiceProvider?.let { TaskListStatusItem.completed() } ?: TaskListStatusItem.notStarted()

    private fun getCriminogenicNeedsStatus(criminogenicNeeds: ReferralCriminogenicNeeds?): TaskListStatusItem = criminogenicNeeds?.let { TaskListStatusItem.completed() } ?: TaskListStatusItem.notStarted()

    private fun getAdditionalSupportNeedsStatus(additionalSupportNeeds: PersonAdditionalSupportNeeds?): TaskListStatusItem {
      return additionalSupportNeeds?.let {
        if (it.additionalSupportNeeded != null && it.interpreterNeeded != null) {
          return TaskListStatusItem.completed()
        }
        return TaskListStatusItem.inProgress()
      } ?: TaskListStatusItem.notStarted()
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
        else -> TaskListStatusItem.inProgress()
      }
    }

    private fun getCheckProbationPractitionerDetailsStatus(
      probationPractitionerDetails: ProbationPractitionerDetailsBffResponseDto?,
      savedProbationPractitionerDetails: ProbationPractitionerDetails?,
    ): TaskListStatusItem? {
      if (probationPractitionerDetails == null) return null

      if (savedProbationPractitionerDetails?.ppDetailsFoundAndCorrect == false) return null

      return savedProbationPractitionerDetails?.let { TaskListStatusItem.completed() } ?: TaskListStatusItem.notStarted()
    }

    private fun getAddMainPointOfContactStatus(
      probationPractitionerDetails: ProbationPractitionerDetailsBffResponseDto?,
      savedProbationPractitionerDetails: ProbationPractitionerDetails?,
    ): TaskListStatusItem? {
      if (probationPractitionerDetails == null) return TaskListStatusItem.notStarted()

      if (savedProbationPractitionerDetails?.ppDetailsFoundAndCorrect == false) return TaskListStatusItem.completed()

      return null
    }
  }
}

data class TaskListStatusItem(
  val completed: Boolean,
  val statusText: String,
  val tag: String? = null,
) {
  companion object {
    fun completed() = TaskListStatusItem(true, "Completed", "govuk-tag--blue")
    fun notStarted() = TaskListStatusItem(false, "Not started", "govuk-tag--grey")
    fun inProgress() = TaskListStatusItem(false, "In progress", "govuk-tag--light-blue")
  }
}
