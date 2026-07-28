package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Person
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.PersonAdditionalSupportNeeds
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Referral

data class TaskListStatusResponseDto(
  val fullName: String,
  val confirmPersonalDetailsCompleted: TaskListStatusItem,
  val checkRiskInformationCompleted: TaskListStatusItem,
  val selectThePersonsNeedsCompleted: TaskListStatusItem,
  val addDetailsOfAnyAdditionalSupportNeedsCompleted: TaskListStatusItem,
  val addDetailsOfMainPointOfContactCompleted: TaskListStatusItem,
) {
  companion object {
    fun from(person: Person, referral: Referral, additionalSupportNeeds: PersonAdditionalSupportNeeds?) = TaskListStatusResponseDto(
      fullName = person.firstName + " " + person.lastName,
      TaskListStatusItem.notStarted(),
      TaskListStatusItem.notStarted(),
      TaskListStatusItem.notStarted(),
      getAdditionalSupportNeedsStatus(additionalSupportNeeds),
      TaskListStatusItem.notStarted(),
    )

    private fun getAdditionalSupportNeedsStatus(additionalSupportNeeds: PersonAdditionalSupportNeeds?): TaskListStatusItem {
      return additionalSupportNeeds?.let {
        if (it.additionalSupportNeeded != null && (it.additionalSupportNeeded == false || it.interpreterNeeded != null)) {
          return TaskListStatusItem.completed()
        }
        return TaskListStatusItem.inProgress()
      } ?: TaskListStatusItem.notStarted()
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
