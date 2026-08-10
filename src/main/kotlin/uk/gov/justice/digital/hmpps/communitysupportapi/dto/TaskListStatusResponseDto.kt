package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import uk.gov.justice.digital.hmpps.communitysupportapi.entity.CommunityServiceProvider
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Person
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.PersonAdditionalSupportNeeds
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Referral
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralCriminogenicNeeds
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.RiskInformation

data class TaskListStatusResponseDto(
  val fullName: String,
  val confirmPersonalDetailsCompleted: TaskListStatusItem,
  val checkRiskInformationCompleted: TaskListStatusItem,
  val selectThePersonsNeedsCompleted: TaskListStatusItem,
  val addDetailsOfAnyAdditionalSupportNeedsCompleted: TaskListStatusItem,
  val addDetailsOfMainPointOfContactCompleted: TaskListStatusItem,
  val selectAnAreaForReferralCompleted: TaskListStatusItem,
) {
  companion object {
    fun from(
      person: Person,
      referral: Referral,
      additionalSupportNeeds: PersonAdditionalSupportNeeds?,
      riskInfo: RiskInformation?,
      criminogenicNeeds: ReferralCriminogenicNeeds?,
      communityServiceProvider: CommunityServiceProvider?,
    ) = TaskListStatusResponseDto(
      fullName = person.firstName + " " + person.lastName,
      TaskListStatusItem.notStarted(),
      getRiskInfoStatus(riskInfo),
      getCriminogenicNeedsStatus(criminogenicNeeds),
      getAdditionalSupportNeedsStatus(additionalSupportNeeds),
      TaskListStatusItem.notStarted(),
      getCommunityServiceProviderStatus(communityServiceProvider),
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
