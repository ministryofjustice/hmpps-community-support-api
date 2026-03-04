package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AppointmentDeliveryMethod
import java.util.UUID

data class AppointmentDeliveryDto(
  val id: UUID,
  val method: AppointmentDeliveryMethod,
  val methodDetails: String?,
  val addressLine1: String?,
  val addressLine2: String?,
  val townOrCity: String?,
  val county: String?,
  val postcode: String?,
)
