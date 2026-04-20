package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AppointmentDeliveryMethod

data class AppointmentDetailsDto(
  val method: AppointmentDeliveryMethod? = null,
  val date: String? = null,
  val time: String? = null,
)
