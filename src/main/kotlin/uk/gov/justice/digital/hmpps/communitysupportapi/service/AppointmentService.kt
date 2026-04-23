package uk.gov.justice.digital.hmpps.communitysupportapi.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.AppointmentDetailsDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.AppointmentIcsFeedbackResponse
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.AppointmentIcsResponse
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.CreateAppointmentRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.CreateIcsFeedbackRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.IcsFeedbackSessionDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.SessionMethodType
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.toDeliveryMethod
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.toDisplayString
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.toLocalTime
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.toSessionDisplayString
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActorType
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Appointment
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AppointmentDelivery
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AppointmentDeliveryMethod
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AppointmentIcs
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AppointmentIcsFeedback
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AppointmentStatusHistory
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AppointmentStatusHistoryType
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AppointmentType
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Referral
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralEvent
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralEventType
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralUser
import uk.gov.justice.digital.hmpps.communitysupportapi.exception.NotFoundException
import uk.gov.justice.digital.hmpps.communitysupportapi.model.CaseIdentifier
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.AppointmentDeliveryRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.AppointmentIcsFeedbackRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.AppointmentIcsRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.AppointmentRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.AppointmentStatusHistoryRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.PersonRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.validation.CaseIdentifierValidator
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

@Service
class AppointmentService(
  private val referralRepository: ReferralRepository,
  private val appointmentRepository: AppointmentRepository,
  private val appointmentDeliveryRepository: AppointmentDeliveryRepository,
  private val appointmentIcsRepository: AppointmentIcsRepository,
  private val appointmentStatusHistoryRepository: AppointmentStatusHistoryRepository,
  private val appointmentIcsFeedbackRepository: AppointmentIcsFeedbackRepository,
  private val personRepository: PersonRepository,
) {
  companion object {
    private val log = LoggerFactory.getLogger(AppointmentService::class.java)
    private val identifierValidator: CaseIdentifierValidator = CaseIdentifierValidator()
  }

  @Transactional
  fun createIcsAppointment(
    caseIdentifier: String,
    request: CreateAppointmentRequest,
    createdBy: ReferralUser,
  ): AppointmentIcsResponse {
    val referral = when (val identifier = identifierValidator.validate(caseIdentifier)) {
      is CaseIdentifier.ReferralId -> referralRepository.findById(identifier.value)
        .orElseThrow { NotFoundException("Referral not found for id ${identifier.value}") }
      is CaseIdentifier.CaseId -> referralRepository.findByReferenceNumber(identifier.value)
        .firstOrNull() ?: throw NotFoundException("Referral not found for reference ${identifier.value}")
    }

    log.info("Creating ICS appointment for referral {}", caseIdentifier)

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

  /**
   * Returns ICS appointment session details by a referral.
   */
  @Transactional(readOnly = true)
  fun getIcsFeedbackSessionDetails(referral: Referral): IcsFeedbackSessionDto {
    val personName = personRepository.findById(referral.personId)
      .orElseThrow { NotFoundException("Person not found for referral ${referral.referenceNumber}") }
      .let { "${it.firstName} ${it.lastName}" }

    val latestIcsAppointment = appointmentIcsRepository
      .findByAppointmentReferralId(referral.id)
      .filter { it.appointment.type == AppointmentType.ICS }
      .maxWithOrNull(
        compareBy<AppointmentIcs> { it.appointmentDateTime }
          .thenBy { it.createdAt },
      )
      ?: throw NotFoundException("ICS appointment not found for referral reference number: ${referral.referenceNumber}")

    val appointmentDetails = AppointmentDetailsDto(
      method = latestIcsAppointment.appointmentDelivery?.method,
      date = latestIcsAppointment.appointmentDateTime.toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
      time = latestIcsAppointment.appointmentDateTime.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")),
    )

    return IcsFeedbackSessionDto(
      fullName = personName,
      appointmentDetails = appointmentDetails,
      otherAppointmentMethods = latestIcsAppointment.sessionCommunication,
    )
  }

  private fun getLatestAppointmentStatus(appointmentId: UUID): AppointmentStatusHistoryType = appointmentStatusHistoryRepository
    .findTopByAppointmentIdOrderByCreatedAtDesc(appointmentId)
    ?.status
    ?: throw IllegalStateException("No status history for appointment $appointmentId")

  private fun getReferralName(appointment: Appointment) = personRepository.findById(appointment.referral.personId).map { it.firstName + " " + it.lastName }.get()

  /**
   * Creates and persists feedback for the given ICS appointment.
   * Verifies that the ICS appointment exists and belongs to the specified referral.
   */
  @Transactional
  fun createIcsFeedback(
    referralId: UUID,
    icsAppointmentId: UUID,
    request: CreateIcsFeedbackRequest,
    submittedBy: ReferralUser,
  ): AppointmentIcsFeedbackResponse {
    val ics = appointmentIcsRepository.findById(icsAppointmentId)
      .orElseThrow { NotFoundException("ICS appointment not found for id $icsAppointmentId") }

    if (ics.appointment.referral.id != referralId) {
      throw NotFoundException("ICS appointment $icsAppointmentId does not belong to referral $referralId")
    }

    log.info("Creating ICS feedback for ics appointment {}", icsAppointmentId)

    val sessionMethod = request.record.howSessionTookPlace
    val isOtherLocation = sessionMethod?.type == SessionMethodType.OTHER_LOCATION

    val feedback = AppointmentIcsFeedback(
      appointmentIcs = ics,
      recordSessionDidSessionHappen = request.record.didSessionHappen,
      recordSessionHowSessionTookPlace = sessionMethod?.type?.toSessionDisplayString(),
      recordSessionNotInPersonReason = sessionMethod?.additionalDetails,
      recordSessionPdu = sessionMethod?.pdu,
      recordSessionAddressLine1 = sessionMethod?.addressLine1.takeIf { isOtherLocation },
      recordSessionAddressLine2 = sessionMethod?.addressLine2.takeIf { isOtherLocation },
      recordSessionTownOrCity = sessionMethod?.townOrCity.takeIf { isOtherLocation },
      recordSessionCounty = sessionMethod?.county.takeIf { isOtherLocation },
      recordSessionPostcode = sessionMethod?.postcode.takeIf { isOtherLocation },
      sessionDetailsWasPersonLate = request.sessionDetails?.wasPersonLate,
      sessionDetailsLateReason = request.sessionDetails?.lateReason,
      sessionDetailsDuration = request.sessionDetails?.duration?.toDisplayString(),
      sessionFeedbackWhatHappened = request.sessionFeedback?.whatHappened,
      sessionFeedbackBehaviour = request.sessionFeedback?.behaviour,
      sessionFeedbackStrengthsIdentified = request.sessionFeedback?.strengthsIdentified,
      issuesConcernsIdentified = request.issuesAndConcerns?.identified,
      issuesConcernsNotifyProbationPractitioner = request.issuesAndConcerns?.notifyProbationPractitioner,
      nextStepsPlannedForNextSession = request.nextSteps?.plannedForNextSession,
      nextStepsActionsBeforeNextSession = request.nextSteps?.actionsBeforeNextSession,
      createdBy = submittedBy,
    )

    val saved = appointmentIcsFeedbackRepository.save(feedback)
    log.info("ICS feedback created with id {} for ics appointment {}", saved.id, icsAppointmentId)

    // Audit: record APPOINTMENT_FEEDBACK_SENT event on the referral
    val referral = referralRepository.findById(referralId)
      .orElseThrow { NotFoundException("Referral not found for id $referralId") }
    val feedbackEvent = ReferralEvent(
      id = UUID.randomUUID(),
      referral = referral,
      eventType = ReferralEventType.APPOINTMENT_FEEDBACK_SENT,
      createdAt = OffsetDateTime.now(),
      actorType = ActorType.EXTERNAL,
      actorId = submittedBy.id,
    )
    referral.addEvent(feedbackEvent)
    referralRepository.save(referral)
    log.info("Referral event APPOINTMENT_FEEDBACK_SENT recorded for referral {}", referralId)

    return AppointmentIcsFeedbackResponse.from(saved)
  }
}
