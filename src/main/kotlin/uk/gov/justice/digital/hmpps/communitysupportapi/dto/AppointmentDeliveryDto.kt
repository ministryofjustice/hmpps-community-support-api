package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AppointmentDeliveryMethod

data class AppointmentDeliveryDto(
  val method: AppointmentDeliveryMethod,
  val methodDetails: String?,
  val addressLine1: String?,
  val addressLine2: String?,
  val townOrCity: String?,
  val county: String?,
  val postcode: String?,
)
