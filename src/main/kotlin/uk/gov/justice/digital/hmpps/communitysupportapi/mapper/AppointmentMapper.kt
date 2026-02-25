package uk.gov.justice.digital.hmpps.communitysupportapi.mapper

import uk.gov.justice.digital.hmpps.communitysupportapi.dto.AppointmentDeliveryDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.AppointmentDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.AppointmentIcsDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.AppointmentStatusHistoryDto
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Appointment
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AppointmentDelivery
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AppointmentIcs
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AppointmentStatusHistory

fun Appointment.toDto() = AppointmentDto(
  id = id,
  referralId = referral.id,
  type = type,
)

fun AppointmentIcs.toDto() = AppointmentIcsDto(
  appointmentDelivery = appointmentDelivery?.toDto(),
  startDate = startDate,
  createdAt = createdAt,
  createdBy = createdBy.toDto(),
  sessionCommunication = sessionCommunication,
)

fun AppointmentDelivery.toDto() = AppointmentDeliveryDto(
  method = method,
  methodDetails = methodDetails,
  addressLine1 = addressLine1,
  addressLine2 = addressLine2,
  townOrCity = townOrCity,
  county = county,
  postcode = postcode,
)

fun AppointmentStatusHistory.toDto() = AppointmentStatusHistoryDto(
  status = status,
  createdAt = createdAt,
)
