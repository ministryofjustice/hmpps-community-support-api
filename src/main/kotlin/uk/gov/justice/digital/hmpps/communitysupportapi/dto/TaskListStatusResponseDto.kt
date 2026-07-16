package uk.gov.justice.digital.hmpps.communitysupportapi.dto

data class TaskListStatusResponseDto(
  val fullName: String,
  val confirmPersonalDetailsCompleted: Boolean,
  val checkRiskInformationCompleted: Boolean,
  val selectThePersonsNeedsCompleted: Boolean,
  val addDetailsOfAnyAdditionalSupportNeedsCompleted: Boolean,
  val addDetailsOfMainPointOfContactCompleted: Boolean,
)
