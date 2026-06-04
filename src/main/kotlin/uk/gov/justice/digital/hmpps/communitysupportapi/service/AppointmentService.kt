package uk.gov.justice.digital.hmpps.communitysupportapi.service

import SessionFeedbackDetailsDto
import org.slf4j.LoggerFactory
import org.springframework.data.jpa.domain.AbstractAuditable_.createdBy
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.AppointmentDetailsDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.AppointmentIcsFeedbackResponse
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.AppointmentIcsResponse
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.CreateAppointmentRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.CreateIcsFeedbackRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.IcsFeedbackSessionDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.ReferralNameDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.SessionMethodRequest
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
import uk.gov.justice.digital.hmpps.communitysupportapi.exception.ConflictException
import uk.gov.justice.digital.hmpps.communitysupportapi.exception.NotFoundException
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.AppointmentDeliveryRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.AppointmentIcsFeedbackRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.AppointmentIcsRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.AppointmentRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.AppointmentStatusHistoryRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.PersonRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralRepository
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

@Service
class AppointmentService(
  private val referralService: ReferralService,
  private val referralAssignmentService: ReferralAssignmentService,
  private val referralRepository: ReferralRepository,
  private val appointmentRepository: AppointmentRepository,
  private val appointmentDeliveryRepository: AppointmentDeliveryRepository,
  private val appointmentIcsRepository: AppointmentIcsRepository,
  private val appointmentStatusHistoryRepository: AppointmentStatusHistoryRepository,
  private val appointmentIcsFeedbackRepository: AppointmentIcsFeedbackRepository,
  private val personRepository: PersonRepository,
  private val referralLookupService: ReferralLookupService,
) {
  companion object {
    private val log = LoggerFactory.getLogger(AppointmentService::class.java)
  }

  private fun createNewAppointment(referral: Referral, type: AppointmentType): Appointment {
    val appointment = Appointment(
      id = UUID.randomUUID(),
      referral = referral,
      type = type,
    )
    return appointmentRepository.save(appointment)
  }

  private fun createAppointmentStatusHistory(
    appointment: Appointment,
    status: AppointmentStatusHistoryType,
    createdAt: LocalDateTime? = null,
  ): AppointmentStatusHistory {
    val history = AppointmentStatusHistory(
      appointment = appointment,
      status = status,
      createdAt = createdAt ?: LocalDateTime.now(),
    )
    return appointmentStatusHistoryRepository.save(history)
  }

  private fun createAppointmentDelivery(sessionMethodRequest: SessionMethodRequest): AppointmentDelivery {
    val deliveryMethod = sessionMethodRequest.type.toDeliveryMethod()

    return AppointmentDelivery(
      id = UUID.randomUUID(),
      method = deliveryMethod,
      methodDetails = sessionMethodRequest.additionalDetails,
      addressLine1 = sessionMethodRequest.addressLine1.takeIf { deliveryMethod == AppointmentDeliveryMethod.IN_PERSON_OTHER_LOCATION },
      addressLine2 = sessionMethodRequest.addressLine2.takeIf { deliveryMethod == AppointmentDeliveryMethod.IN_PERSON_OTHER_LOCATION },
      townOrCity = sessionMethodRequest.townOrCity.takeIf { deliveryMethod == AppointmentDeliveryMethod.IN_PERSON_OTHER_LOCATION },
      county = sessionMethodRequest.county.takeIf { deliveryMethod == AppointmentDeliveryMethod.IN_PERSON_OTHER_LOCATION },
      postcode = sessionMethodRequest.postcode.takeIf { deliveryMethod == AppointmentDeliveryMethod.IN_PERSON_OTHER_LOCATION },
    ).also {
      appointmentDeliveryRepository.save(it)
    }
  }

  private fun createAppointmentIcsRecord(
    appointment: Appointment,
    appointmentDelivery: AppointmentDelivery,
    request: CreateAppointmentRequest,
    createdBy: ReferralUser,
  ): AppointmentIcs {
    val startDateTime = LocalDateTime.of(request.date, request.time.toLocalTime())

    val ics = AppointmentIcs(
      id = UUID.randomUUID(),
      appointment = appointment,
      appointmentDelivery = appointmentDelivery,
      appointmentDateTime = startDateTime,
      createdBy = createdBy,
      sessionCommunication = request.sessionCommunication,
      changeRequestedBy = request.changeAppointmentDetails?.changeRequestedBy,
      changeReason = request.changeAppointmentDetails?.reasonForChange,
    )

    return appointmentIcsRepository.save(ics)
  }

  @Transactional
  fun createIcsAppointment(
    caseIdentifier: String,
    request: CreateAppointmentRequest,
    createdBy: ReferralUser,
  ): AppointmentIcsResponse {
    log.info("Creating ICS appointment for referral {}", caseIdentifier)

    val referral = referralLookupService.findByCaseIdentifier(caseIdentifier)

    // 1. Appointment (parent record)
    val appointment = createNewAppointment(referral, AppointmentType.ICS)

    // 2. Status History
    val appointmentHistory = createAppointmentStatusHistory(appointment, AppointmentStatusHistoryType.SCHEDULED)

    // 3. Delivery method
    val appointmentDelivery = createAppointmentDelivery(request.sessionMethodRequest)

    // 4. Combine date + time
    val localTime = request.time.toLocalTime()
    val startDateTime = LocalDateTime.of(request.date, localTime)

    // 5. ICS record
    val savedIcs = createAppointmentIcsRecord(
      appointment = appointment,
      appointmentDelivery = appointmentDelivery,
      request = request,
      createdBy = createdBy,
    )

    log.info("ICS appointment created with id {}", savedIcs.id)
    return AppointmentIcsResponse.from(savedIcs, appointmentHistory.status, getReferralName(savedIcs.appointment))
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
   * Returns the latest ICS appointment by its referral ID.
   */
  @Transactional(readOnly = true)
  fun getLatestIcsAppointment(caseIdentifier: String): AppointmentIcsResponse {
    val referral = referralLookupService.findByCaseIdentifier(caseIdentifier)
    val ics = appointmentIcsRepository.findLatestIcsByReferralId(referral.id, AppointmentType.ICS)
    if (ics === null) {
      throw NotFoundException("ICS appointment not found: $caseIdentifier")
    }
    return AppointmentIcsResponse.from(ics, getLatestAppointmentStatus(ics.appointment.id), getReferralName(ics.appointment))
  }

  fun changeIcsAppointment(
    caseIdentifier: String,
    request: CreateAppointmentRequest,
    changedBy: ReferralUser,
  ): AppointmentIcsResponse {
    log.info("Changing ICS appointment for referral {}", caseIdentifier)

    val referral = referralLookupService.findByCaseIdentifier(caseIdentifier)

    // Fetch the specific history record
    val existingIcs = appointmentIcsRepository.findLatestIcsByReferralId(referral.id, AppointmentType.ICS)

    if (existingIcs === null) {
      throw NotFoundException("ICS appointment not found: $caseIdentifier")
    }

    // 1. update previous ics appointment status History
    val existingIcsAppointmentHistory = createAppointmentStatusHistory(
      appointment = existingIcs.appointment,
      status = AppointmentStatusHistoryType.RESCHEDULED,
      createdAt = existingIcs.createdAt,
    )

    // 2. Create the new appointment (parent record)
    val appointment = createNewAppointment(referral, AppointmentType.ICS)

    // 3. update the new ics appointment status History
    val newIcsAppointmentHistory = createAppointmentStatusHistory(appointment, AppointmentStatusHistoryType.SCHEDULED)

    // 4. create new delivery method
    val appointmentDelivery = createAppointmentDelivery(request.sessionMethodRequest)

    // 5. create new ics record
    val savedIcs = createAppointmentIcsRecord(
      appointment = appointment,
      appointmentDelivery = appointmentDelivery,
      request = request,
      createdBy = changedBy,
    )

    log.info("ICS appointment updated with id {}", savedIcs.id)
    return AppointmentIcsResponse.from(savedIcs, newIcsAppointmentHistory.status, getReferralName(savedIcs.appointment))
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

  private fun getReferralName(appointment: Appointment) = personRepository.findById(appointment.referral.personId).map {
    ReferralNameDto(
      it.firstName,
      it.lastName,
    )
  }.get()

  /**
   * Returns a single ICS feedback record by its own ID.
   */
  @Transactional(readOnly = true)
  fun getIcsFeedback(icsFeedbackId: UUID): AppointmentIcsFeedbackResponse {
    val icsFeedback = appointmentIcsFeedbackRepository.findById(icsFeedbackId)
      .orElseThrow { NotFoundException("ICS feedback not found for id $icsFeedbackId") }

    val feedbackSubmittedBy = icsFeedback.createdBy?.let { "${it.fullName} (${it.hmppsAuthUsername})" } ?: "Unknown user"

    val ics = icsFeedback.appointmentIcs

    val person = personRepository.findById(ics.appointment.referral.personId)
      .orElseThrow { NotFoundException("Person not found for referral ${ics.appointment.referral.personId}") }

    val caseReference = ics.appointment.referral.referenceNumber
      ?: throw IllegalStateException("Referral ${ics.appointment.referral.id} does not have a reference number")

    val caseWorkers = referralAssignmentService.getAssignedCaseWorkers(caseReference)
      ?.map { "${it.fullName} (${it.emailAddress})" }
      ?.takeIf { it.isNotEmpty() }
      ?: throw NotFoundException("Case workers not found for referral $caseReference")

    val sessionDetails = SessionFeedbackDetailsDto(
      currentCaseworkers = caseWorkers,
      feedbackSubmittedBy = feedbackSubmittedBy,
      startDateTime = ics.appointmentDateTime,
      sessionMethod = ics.appointmentDelivery?.method,
      sessionCommunications = ics.sessionCommunication,
      personFirstName = person.firstName,
    )

    return AppointmentIcsFeedbackResponse.from(icsFeedback, sessionDetails)
  }

  /**
   * Creates and persists feedback for the given ICS appointment.
   * Verifies that the ICS appointment exists and belongs to the specified referral.
   * If feedback already exists for this ICS appointment, throws ConflictException.
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

    // Check if feedback already exists and throw ConflictException if it does
    appointmentIcsFeedbackRepository.findByAppointmentIcsId(icsAppointmentId)?.let { existing ->
      log.warn("ICS feedback already exists for ics appointment {}, existing record id: {}", icsAppointmentId, existing.id)
      throw ConflictException("ICS feedback already exists for appointment $icsAppointmentId")
    }

    log.info("Creating ICS feedback for ics appointment {}", icsAppointmentId)

    val sessionMethod = request.record.howSessionTookPlace
    val isOtherLocation = sessionMethod?.type == SessionMethodType.IN_PERSON_OTHER_LOCATION

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
      recordSessionDidPersonAttend = request.record.didPersonAttend,
      recordSessionNotHappenReason = request.record.sessionNotHappenReason?.reason?.name,
      recordSessionNotHappenReasonDetails = request.record.sessionNotHappenReason?.details,
      recordSessionNoAttendanceInformation = request.record.noAttendanceInformation,
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

    // Derive and persist the new appointment status from the feedback answers
    val newStatus = deriveAppointmentStatus(saved)
    val statusHistory = AppointmentStatusHistory(
      appointment = ics.appointment,
      status = newStatus,
    )
    appointmentStatusHistoryRepository.save(statusHistory)
    log.info("Appointment status set to {} for appointment {}", newStatus, ics.appointment.id)

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

  /**
   * Derives the [AppointmentStatusHistoryType] from the submitted feedback:
   *
   * - `didSessionHappen == true`                                                     → COMPLETED
   * - `didSessionHappen == false` AND `didPersonAttend == false`                     → DID_NOT_ATTEND
   * - `didSessionHappen == false` AND `didPersonAttend == true`
   *   AND `sessionNotHappenReason != null`                                           → DID_NOT_HAPPEN
   */
  private fun deriveAppointmentStatus(feedback: AppointmentIcsFeedback): AppointmentStatusHistoryType = when {
    feedback.recordSessionDidSessionHappen -> AppointmentStatusHistoryType.COMPLETED
    feedback.recordSessionDidPersonAttend == false -> AppointmentStatusHistoryType.DID_NOT_ATTEND
    else -> AppointmentStatusHistoryType.DID_NOT_HAPPEN
  }
}
