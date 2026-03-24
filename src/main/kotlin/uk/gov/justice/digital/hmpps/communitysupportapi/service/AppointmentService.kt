package uk.gov.justice.digital.hmpps.communitysupportapi.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.AppointmentIcsResponse
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.CreateAppointmentRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.toDeliveryMethod
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.toLocalTime
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Appointment
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AppointmentDelivery
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AppointmentDeliveryMethod
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AppointmentIcs
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AppointmentStatusHistory
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AppointmentStatusHistoryType
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AppointmentType
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralUser
import uk.gov.justice.digital.hmpps.communitysupportapi.exception.NotFoundException
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.AppointmentDeliveryRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.AppointmentIcsRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.AppointmentRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.AppointmentStatusHistoryRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.PersonRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralRepository
import java.time.LocalDateTime
import java.util.UUID

@Service
class AppointmentService(
  private val referralRepository: ReferralRepository,
  private val appointmentRepository: AppointmentRepository,
  private val appointmentDeliveryRepository: AppointmentDeliveryRepository,
  private val appointmentIcsRepository: AppointmentIcsRepository,
  private val appointmentStatusHistoryRepository: AppointmentStatusHistoryRepository,
  private val personRepository: PersonRepository,
) {
  companion object {
    private val log = LoggerFactory.getLogger(AppointmentService::class.java)
  }

  @Transactional
  fun createIcsAppointment(
    referralId: UUID,
    request: CreateAppointmentRequest,
    createdBy: ReferralUser,
  ): AppointmentIcsResponse {
    val referral = referralRepository.findById(referralId)
      .orElseThrow { NotFoundException("Referral not found for id $referralId") }

    log.info("Creating ICS appointment for referral {}", referralId)

    // 1. Appointment (parent record)
    val appointment = Appointment(
      id = UUID.randomUUID(),
      referral = referral,
      type = AppointmentType.ICS,
    )
    appointmentRepository.save(appointment)

    // 2. Status History
    val appointmentHistory = AppointmentStatusHistory(
      appointment = appointment,
      status = AppointmentStatusHistoryType.SCHEDULED,
    )
    appointmentStatusHistoryRepository.save(appointmentHistory)

    // 3. Delivery method
    val deliveryMethod = request.sessionMethodRequest.type.toDeliveryMethod()
    val appointmentDelivery = AppointmentDelivery(
      id = UUID.randomUUID(),
      method = deliveryMethod,
      methodDetails = request.sessionMethodRequest.additionalDetails,
      // Address fields are only relevant for IN_PERSON_OTHER_LOCATION
      addressLine1 = request.sessionMethodRequest.addressLine1.takeIf { deliveryMethod == AppointmentDeliveryMethod.IN_PERSON_OTHER_LOCATION },
      addressLine2 = request.sessionMethodRequest.addressLine2.takeIf { deliveryMethod == AppointmentDeliveryMethod.IN_PERSON_OTHER_LOCATION },
      townOrCity = request.sessionMethodRequest.townOrCity.takeIf { deliveryMethod == AppointmentDeliveryMethod.IN_PERSON_OTHER_LOCATION },
      county = request.sessionMethodRequest.county.takeIf { deliveryMethod == AppointmentDeliveryMethod.IN_PERSON_OTHER_LOCATION },
      postcode = request.sessionMethodRequest.postcode.takeIf { deliveryMethod == AppointmentDeliveryMethod.IN_PERSON_OTHER_LOCATION },
    )
    appointmentDeliveryRepository.save(appointmentDelivery)

    // 4. Combine date + time
    val localTime = request.time.toLocalTime()
    val startDateTime = LocalDateTime.of(request.date, localTime)

    // 5. ICS record
    val ics = AppointmentIcs(
      id = UUID.randomUUID(),
      appointment = appointment,
      appointmentDelivery = appointmentDelivery,
      appointmentDateTime = startDateTime,
      createdBy = createdBy,
      sessionCommunication = request.sessionCommunication,
    )
    val savedIcs = appointmentIcsRepository.save(ics)

    log.info("ICS appointment created with id {}", savedIcs.id)
    return AppointmentIcsResponse.from(savedIcs, appointmentHistory.status, getReferralName(ics.appointment))
  }

  /**
   * Returns all ICS appointments for the given referral, mapped to the UI-friendly response DTO.
   */
  @Transactional(readOnly = true)
  fun getIcsAppointmentsByReferral(referralId: UUID): List<AppointmentIcsResponse> {
    // Verify the referral exists
    if (!referralRepository.existsById(referralId)) {
      throw NotFoundException("Referral not found for id $referralId")
    }
    return appointmentIcsRepository.findByAppointmentReferralId(referralId)
      .map { ics ->
        AppointmentIcsResponse.from(
          ics,
          getLatestAppointmentStatus(ics.appointment.id),
          getReferralName(ics.appointment),
        )
      }
  }

  /**
   * Returns a single ICS appointment by its own ID.
   */
  @Transactional(readOnly = true)
  fun getIcsAppointment(icsId: UUID): AppointmentIcsResponse {
    val ics = appointmentIcsRepository.findById(icsId)
      .orElseThrow { NotFoundException("Appointment ICS not found for id $icsId") }
    return AppointmentIcsResponse.from(ics, getLatestAppointmentStatus(ics.appointment.id), getReferralName(ics.appointment))
  }

  private fun getLatestAppointmentStatus(appointmentId: UUID): AppointmentStatusHistoryType = appointmentStatusHistoryRepository
    .findTopByAppointmentIdOrderByCreatedAtDesc(appointmentId)
    ?.status
    ?: throw IllegalStateException("No status history for appointment $appointmentId")

  private fun getReferralName(appointment: Appointment) = personRepository.findById(appointment.referral.personId).map { it.firstName + " " + it.lastName }.get()
}
