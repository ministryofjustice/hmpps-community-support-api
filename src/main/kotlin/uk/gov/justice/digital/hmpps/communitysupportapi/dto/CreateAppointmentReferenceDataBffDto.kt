package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import uk.gov.justice.digital.hmpps.communitysupportapi.model.ProbationOfficeSummary

data class CreateAppointmentReferenceDataBffDto(
  val appointmentTypes: List<CreateAppointmentTypeOptionDto>,
  val probationOfficeLocations: List<ProbationOfficeSummary>,
)

data class CreateAppointmentTypeOptionDto(
  val name: String,
  val value: String,
)
