package uk.gov.justice.digital.hmpps.communitysupportapi.dto

data class TaskListStatusResponseDto(
  val confirmPersonalDetails: Boolean,
  val checkRiskInformation: Boolean,
  val selectThePersonsNeeds: Boolean,
  val addDetailsOfAnyAdditionalSupportNeeds: Boolean,
  val addDetailsOfMainPointOfContact: Boolean,
  val checkAnswers: Boolean,
)
