package uk.gov.justice.digital.hmpps.communitysupportapi.service

import SessionFeedbackAppointmentDetailsDto
import jakarta.validation.ValidationException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.AppointmentDetailsDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.AppointmentIcsFeedbackResponse
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.AppointmentIcsResponse
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.CaseWorkerSummaryDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.CreateAppointmentReferenceDataBffDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.CreateAppointmentRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.CreateAppointmentTypeOptionDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.CreateIcsFeedbackRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.ExistingSupportNeedsBffResponseDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.IcsFeedbackAttendanceBffDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.IcsFeedbackCheckAnswersBffDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.IcsFeedbackSessionDetailsBffDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.IcsFeedbackSessionDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.ReferralAppointmentSummaryDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.ReferralAppointmentsBffResponseDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.ReferralAppointmentsPersonDetailsDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.ReferralNameDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.SessionMethod
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.SessionMethodRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.SessionMethodType
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.toAppointmentTimeResponse
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.toDeliveryMethod
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.toDisplayLabel
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.toLocalTime
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Appointment
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AppointmentDelivery
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AppointmentDeliveryMethod
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AppointmentHistory
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AppointmentStatusHistory
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AppointmentType
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ChangeRequesterType
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Referral
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralUser
import uk.gov.justice.digital.hmpps.communitysupportapi.exception.ConflictException
import uk.gov.justice.digital.hmpps.communitysupportapi.exception.NotFoundException
import uk.gov.justice.digital.hmpps.communitysupportapi.factory.ReferralEventFactory
import uk.gov.justice.digital.hmpps.communitysupportapi.model.AppointmentStatusHistoryType
import uk.gov.justice.digital.hmpps.communitysupportapi.model.ProbationOfficeSummary
import uk.gov.justice.digital.hmpps.communitysupportapi.model.getAppointmentStatusHistoryType
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.AppointmentDeliveryRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.AppointmentIcsFeedbackRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.AppointmentIcsRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.AppointmentRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.AppointmentStatusHistoryRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.PersonAdditionalSupportNeedsRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.PersonRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralRepository
import java.time.Clock
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

@Service
class AppointmentService(
  private val referralAssignmentService: ReferralAssignmentService,
  private val referenceDataService: ReferenceDataService,
  private val referralRepository: ReferralRepository,
  private val appointmentRepository: AppointmentRepository,
  private val appointmentDeliveryRepository: AppointmentDeliveryRepository,
  private val appointmentIcsRepository: AppointmentIcsRepository,
  private val appointmentStatusHistoryRepository: AppointmentStatusHistoryRepository,
  private val appointmentIcsFeedbackRepository: AppointmentIcsFeedbackRepository,
  private val personRepository: PersonRepository,
  private val referralLookupService: ReferralLookupService,
  private val personAdditionalSupportNeedsRepository: PersonAdditionalSupportNeedsRepository,
  private val clock: Clock,
) {
  companion object {
    private val log = LoggerFactory.getLogger(AppointmentService::class.java)

    private val SUPPORTED_CREATE_APPOINTMENT_TYPES = listOf(
      AppointmentType.CONTACT_SESSION,
      AppointmentType.ICS,
      AppointmentType.HANDOVER_SESSION,
      AppointmentType.POST_RELEASE_SESSION,
      AppointmentType.PRE_RELEASE_SESSION,
    )

    private val CREATE_APPOINTMENT_TYPE_OPTIONS = SUPPORTED_CREATE_APPOINTMENT_TYPES.map {
      CreateAppointmentTypeOptionDto(
        name = it.toDisplayLabel(),
        value = it.toCreateAppointmentValue(),
      )
    }

    private val ALL_LOCATION_METHODS = setOf(
      SessionMethodType.PHONE,
      SessionMethodType.VIDEO,
      SessionMethodType.IN_PERSON_PROBATION_OFFICE,
      SessionMethodType.IN_PERSON_OTHER_LOCATION,
    )

    /** Session methods permitted for each appointment type (see session-mapping.png). */
    private val ALLOWED_SESSION_METHODS: Map<AppointmentType, Set<SessionMethodType>> = mapOf(
      AppointmentType.ICS to ALL_LOCATION_METHODS,
      AppointmentType.CONTACT_SESSION to ALL_LOCATION_METHODS,
      AppointmentType.POST_RELEASE_SESSION to ALL_LOCATION_METHODS,
      AppointmentType.PRE_RELEASE_SESSION to setOf(
        SessionMethodType.PHONE,
        SessionMethodType.VIDEO,
        SessionMethodType.IN_PERSON_OTHER_LOCATION,
        SessionMethodType.IN_PERSON_PRISON,
      ),
      AppointmentType.HANDOVER_SESSION to setOf(
        SessionMethodType.PHONE,
        SessionMethodType.VIDEO,
        SessionMethodType.IN_PERSON,
      ),
    )
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
    val prison = if (sessionMethodRequest.type == SessionMethodType.IN_PERSON_PRISON) {
      val code = sessionMethodRequest.prisonCode?.trim()
        ?.takeIf { it.isNotEmpty() }
        ?: throw ValidationException("prisonCode is required for IN_PERSON_PRISON")
      referenceDataService.getPrisons().firstOrNull { it.code == code }
        ?: throw ValidationException("Prison $code is not an active prison")
    } else {
      null
    }

    return AppointmentDelivery(
      id = UUID.randomUUID(),
      method = deliveryMethod,
      methodDetails = prison?.description ?: sessionMethodRequest.additionalDetails,
      prisonCode = prison?.code,
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
    createdAt: LocalDateTime = LocalDateTime.now(),
  ): AppointmentHistory {
    val startDateTime = LocalDateTime.of(request.date, request.time.toLocalTime())

    val ics = AppointmentHistory(
      id = UUID.randomUUID(),
      appointment = appointment,
      appointmentDelivery = appointmentDelivery,
      appointmentDateTime = startDateTime,
      meetAtTheGate = request.meetAtTheGate,
      createdBy = createdBy,
      createdAt = createdAt,
      sessionCommunication = request.sessionCommunication,
      changeRequestedBy = request.changeAppointmentDetails?.changeRequestedBy,
      changeReason = request.changeAppointmentDetails?.reasonForChange,
    )

    return appointmentIcsRepository.save(ics)
  }

  private fun updateAppointmentIcsRecord(
    ics: AppointmentHistory,
    changeRequestedBy: ChangeRequesterType?,
    changeReason: String?,
  ): AppointmentHistory {
    ics.changeRequestedBy = changeRequestedBy
    ics.changeReason = changeReason

    return appointmentIcsRepository.saveAndFlush(ics)
  }

  /**
   * Creates an appointment of the given [type] for a referral. Used by both the legacy
   * ICS endpoint (which always passes [AppointmentType.ICS]) and the generic appointment endpoint.
   */
  @Transactional
  fun createAppointment(
    caseIdentifier: String,
    type: AppointmentType,
    request: CreateAppointmentRequest,
    createdBy: ReferralUser,
  ): AppointmentIcsResponse {
    log.info("Creating {} appointment for referral {}", type, caseIdentifier)

    validateAppointmentRequest(type, request)

    val referral = referralLookupService.findByCaseIdentifier(caseIdentifier)

    // 1. Appointment (parent record)
    val appointment = createNewAppointment(referral, type)

    // 2. Status History
    val appointmentHistory = createAppointmentStatusHistory(appointment, AppointmentStatusHistoryType.SCHEDULED)

    // 3. Delivery method
    val appointmentDelivery = createAppointmentDelivery(request.sessionMethodRequest)

    // 4. Appointment history record
    val savedIcs = createAppointmentIcsRecord(
      appointment = appointment,
      appointmentDelivery = appointmentDelivery,
      request = request,
      createdBy = createdBy,
      createdAt = appointmentHistory.createdAt,
    )

    log.info("{} appointment created with id {}", type, savedIcs.id)
    return AppointmentIcsResponse.from(savedIcs, appointmentHistory.status, getReferralName(savedIcs.appointment))
  }

  /**
   * Validates the request against the form rules for the given appointment type (see session-mapping.png).
   */
  private fun validateAppointmentRequest(type: AppointmentType, request: CreateAppointmentRequest) {
    val sessionMethod = request.sessionMethodRequest

    val allowedMethods = ALLOWED_SESSION_METHODS.getValue(type)
    if (sessionMethod.type !in allowedMethods) {
      throw ValidationException("Session method ${sessionMethod.type} is not allowed for appointment type $type")
    }

    if (sessionMethod.type == SessionMethodType.IN_PERSON_OTHER_LOCATION) {
      val missingFields = listOfNotNull(
        "addressLine1".takeIf { sessionMethod.addressLine1.isNullOrBlank() },
        "townOrCity".takeIf { sessionMethod.townOrCity.isNullOrBlank() },
        "postcode".takeIf { sessionMethod.postcode.isNullOrBlank() },
      )
      if (missingFields.isNotEmpty()) {
        throw ValidationException("Missing required address fields for IN_PERSON_OTHER_LOCATION: ${missingFields.joinToString()}")
      }
    }

    if (sessionMethod.type == SessionMethodType.IN_PERSON_PRISON && sessionMethod.prisonCode.isNullOrBlank()) {
      throw ValidationException("prisonCode is required for IN_PERSON_PRISON")
    }

    if (type == AppointmentType.POST_RELEASE_SESSION) {
      if (request.meetAtTheGate == null) {
        throw ValidationException("meetAtTheGate is required for appointment type $type")
      }
    } else if (request.meetAtTheGate != null) {
      throw ValidationException("meetAtTheGate is only applicable to appointment type ${AppointmentType.POST_RELEASE_SESSION}")
    }

    if (type == AppointmentType.HANDOVER_SESSION && request.sessionCommunication.isNotEmpty()) {
      throw ValidationException("sessionCommunication is not applicable to appointment type $type")
    }
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
    return appointmentIcsRepository.findByAppointmentReferralIdOrderByCreatedAtDesc(referralId)
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

    if (request.sessionMethodRequest.type == SessionMethodType.IN_PERSON_PRISON) {
      throw ValidationException("Session method IN_PERSON_PRISON is not allowed for appointment type ICS")
    }

    val referral = referralLookupService.findByCaseIdentifier(caseIdentifier)

    // Fetch the specific history record
    val existingIcs = appointmentIcsRepository.findLatestIcsByReferralId(referral.id, AppointmentType.ICS)

    if (existingIcs === null) {
      throw NotFoundException("ICS appointment not found: $caseIdentifier")
    }

    // 1. update previous ics appointment status History
    createAppointmentStatusHistory(
      appointment = existingIcs.appointment,
      status = AppointmentStatusHistoryType.CHANGED,
      createdAt = existingIcs.createdAt,
    )

    // 2. update appointment ics history row
    updateAppointmentIcsRecord(
      existingIcs,
      request.changeAppointmentDetails?.changeRequestedBy,
      request.changeAppointmentDetails?.reasonForChange,
    )

    // 3. remove changeAppointmentDetails from request
    request.changeAppointmentDetails = null

    // 4. Create the new appointment (parent record)
    val appointment = createNewAppointment(referral, AppointmentType.ICS)

    // 5. update the new ics appointment status History
    val newIcsAppointmentHistory = createAppointmentStatusHistory(appointment, AppointmentStatusHistoryType.SCHEDULED)

    // 6. create new delivery method
    val appointmentDelivery = createAppointmentDelivery(request.sessionMethodRequest)

    // 7. create new ics record
    val savedIcs = createAppointmentIcsRecord(
      appointment = appointment,
      appointmentDelivery = appointmentDelivery,
      request = request,
      createdBy = changedBy,
      createdAt = newIcsAppointmentHistory.createdAt,
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

    val appointmentHistory = getLatestIcsAppointmentHistory(referral)

    val appointmentDetails = AppointmentDetailsDto(
      method = appointmentHistory.appointmentDelivery?.method,
      date = appointmentHistory.appointmentDateTime.toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
      time = appointmentHistory.appointmentDateTime.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")),
    )

    return IcsFeedbackSessionDto(
      fullName = personName,
      appointmentDetails = appointmentDetails,
      otherAppointmentMethods = appointmentHistory.sessionCommunication,
    )
  }

  /**
   * Returns the latest ICS appointment history entry for a referral.
   */
  private fun getLatestIcsAppointmentHistory(referral: Referral): AppointmentHistory {
    val appointments = appointmentRepository
      .findAllByReferralId(referral.id)
      .orEmpty()
      .filter { it.type == AppointmentType.ICS }
      .ifEmpty { throw NotFoundException("No ICS appointments found for referral reference number: ${referral.referenceNumber}") }

    return appointments
      .flatMap { it.appointmentHistory }
      .maxWithOrNull(
        compareBy<AppointmentHistory> { it.appointmentDateTime }
          .thenBy { it.createdAt },
      )
      ?: throw NotFoundException("ICS appointment not found for referral reference number: ${referral.referenceNumber}")
  }

  /**
   * Returns the referral's first name and the latest ICS appointment date and time for the attendance page.
   */
  @Transactional(readOnly = true)
  fun getIcsFeedbackAttendance(referral: Referral): IcsFeedbackAttendanceBffDto {
    val referralFirstName = getIcsFeedbackReferralFirstName(referral)
    val appointmentHistory = getLatestIcsAppointmentHistory(referral)

    return IcsFeedbackAttendanceBffDto(
      referralFirstName = referralFirstName,
      date = appointmentHistory.appointmentDateTime.toLocalDate(),
      time = appointmentHistory.appointmentDateTime.toLocalTime().toAppointmentTimeResponse(),
    )
  }

  /**
   * Returns the referral's first name and the latest ICS appointment date and time for the session details page.
   */
  @Transactional(readOnly = true)
  fun getIcsFeedbackSessionDetailsPage(referral: Referral): IcsFeedbackSessionDetailsBffDto {
    val referralFirstName = getIcsFeedbackReferralFirstName(referral)
    val appointmentHistory = getLatestIcsAppointmentHistory(referral)

    return IcsFeedbackSessionDetailsBffDto(
      referralFirstName = referralFirstName,
      date = appointmentHistory.appointmentDateTime.toLocalDate(),
      time = appointmentHistory.appointmentDateTime.toLocalTime().toAppointmentTimeResponse(),
    )
  }

  /**
   * Returns the referral's first name and latest ICS appointment ID for the check answers page.
   */
  @Transactional(readOnly = true)
  fun getIcsFeedbackCheckAnswers(referral: Referral): IcsFeedbackCheckAnswersBffDto {
    val referralFirstName = getIcsFeedbackReferralFirstName(referral)
    val appointmentHistory = getLatestIcsAppointmentHistory(referral)

    return IcsFeedbackCheckAnswersBffDto(
      referralFirstName = referralFirstName,
      appointmentIcsId = appointmentHistory.id,
    )
  }

  /**
   * Returns the referral's first name for the ICS feedback pages.
   */
  @Transactional(readOnly = true)
  fun getIcsFeedbackReferralFirstName(referral: Referral): String = personRepository.findById(referral.personId)
    .orElseThrow { NotFoundException("Person not found for referral ${referral.referenceNumber}") }
    .firstName

  /**
   * Returns the session method of the latest ICS appointment for a referral.
   */
  @Transactional(readOnly = true)
  fun getIcsFeedbackSessionMethod(referral: Referral): SessionMethod = getIcsAppointmentsByReferral(referral.id).firstOrNull()?.sessionMethod
    ?: throw NotFoundException("No ICS appointments found for referral reference number: ${referral.referenceNumber}")

  /**
   * Returns ICS appointment existing support needs by a referral.
   */
  @Transactional(readOnly = true)
  fun getReferralExistingAdditionalSupportNeeds(referral: Referral): ExistingSupportNeedsBffResponseDto {
    val referralFirstName = personRepository.findById(referral.personId)
      .orElseThrow { NotFoundException("Person not found for referral ${referral.referenceNumber}") }.firstName
    val referralId = referral.id
    val personAdditionalSupportNeeds = personAdditionalSupportNeedsRepository.findByReferralId(referralId)
      ?: throw NotFoundException("Existing support needs not found for referral $referralId")

    return ExistingSupportNeedsBffResponseDto(
      personAdditionalSupportNeeds.physicalHealthDetails,
      personAdditionalSupportNeeds.mentalEmotionalHealthDetails,
      personAdditionalSupportNeeds.neurodiversityDetails,
      personAdditionalSupportNeeds.locationTravelDetails,
      personAdditionalSupportNeeds.caringResponsibilitiesDetails,
      personAdditionalSupportNeeds.employmentResponsibilitiesDetails,
      personAdditionalSupportNeeds.diversityDetails,
      personAdditionalSupportNeeds.anythingElseDetails,
      personAdditionalSupportNeeds.interpreterLanguage,
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

    val feedbackSubmittedBy = icsFeedback.createdBy?.let {
      CaseWorkerSummaryDto(fullName = it.fullName, emailAddress = it.hmppsAuthUsername)
    } ?: CaseWorkerSummaryDto(fullName = "Unknown user", emailAddress = "Unknown email address")

    val ics = icsFeedback.appointmentIcs

    val person = personRepository.findById(ics.appointment.referral.personId)
      .orElseThrow { NotFoundException("Person not found for referral ${ics.appointment.referral.personId}") }

    val caseReference = ics.appointment.referral.referenceNumber
      ?: throw IllegalStateException("Referral ${ics.appointment.referral.id} does not have a reference number")

    val caseWorkers = referralAssignmentService.getAssignedCaseWorkers(caseReference)
      ?.map { CaseWorkerSummaryDto(fullName = it.fullName, emailAddress = it.emailAddress) }
      ?.takeIf { it.isNotEmpty() }
      ?: throw NotFoundException("Case workers not found for referral $caseReference")

    val sessionFeedbackAppointmentDetails = SessionFeedbackAppointmentDetailsDto(
      currentCaseworkers = caseWorkers,
      feedbackSubmittedBy = feedbackSubmittedBy,
      startDateTime = ics.appointmentDateTime,
      appointmentDeliveryDetails = ics.appointmentDelivery,
      sessionCommunications = ics.sessionCommunication,
      personFirstName = person.firstName,
    )

    return AppointmentIcsFeedbackResponse.from(icsFeedback, sessionFeedbackAppointmentDetails)
  }

  /**
   * Creates and persists feedback for the given ICS appointment.
   * Verifies that the ICS appointment exists and belongs to the specified referral.
   * If feedback already exists for this ICS appointment, throws ConflictException.
   */
  @Transactional
  fun createIcsFeedback(
    caseReference: String,
    icsAppointmentId: UUID,
    request: CreateIcsFeedbackRequest,
    submittedBy: ReferralUser,
  ): AppointmentIcsFeedbackResponse {
    // Validate the entities
    val referral = referralLookupService.findByCaseIdentifier(caseReference)

    val ics = appointmentIcsRepository.findById(icsAppointmentId)
      .orElseThrow { NotFoundException("ICS appointment not found for id $icsAppointmentId") }

    if (ics.appointment.referral.id != referral.id) {
      throw NotFoundException("ICS appointment $icsAppointmentId does not belong to referral $caseReference")
    }

    appointmentIcsFeedbackRepository.findByAppointmentIcsId(icsAppointmentId)?.let { existing ->
      log.warn("ICS feedback already exists for ics appointment {}, existing record id: {}", icsAppointmentId, existing.id)
      throw ConflictException("ICS feedback already exists for appointment $icsAppointmentId")
    }

    // Create and persist the feedback record
    log.info("Creating ICS feedback for ics appointment {}", icsAppointmentId)

    val savedAppointmentHistory = appointmentIcsFeedbackRepository.save(request.toAppointmentIcsFeedback(ics, submittedBy))
    log.info("ICS feedback created with id {} for ics appointment {}", savedAppointmentHistory.id, icsAppointmentId)

    val savedAppointmentStatusHistory = appointmentStatusHistoryRepository.save(
      AppointmentStatusHistory(
        appointment = ics.appointment,
        status = savedAppointmentHistory.getAppointmentStatusHistoryType(),
      ),
    )
    log.info("Appointment status set to {} for appointment {}", savedAppointmentStatusHistory.status, ics.appointment.id)

    referral.addEvent(ReferralEventFactory(referral).appointmentFeedbackSent(submittedBy.id))
    referralRepository.save(referral)
    log.info("Referral event APPOINTMENT_FEEDBACK_SENT recorded for referral {}", referral.id)

    return AppointmentIcsFeedbackResponse.from(savedAppointmentHistory)
  }

  /**
   * Returns page data for create appointment, including supported appointment types and probation office locations.
   */
  @Transactional(readOnly = true)
  fun getCreateAppointmentReferenceData(caseReference: String): CreateAppointmentReferenceDataBffDto {
    referralLookupService.findByCaseIdentifier(caseReference)

    return CreateAppointmentReferenceDataBffDto(
      appointmentTypes = CREATE_APPOINTMENT_TYPE_OPTIONS,
      probationOfficeLocations = referenceDataService.getProbationOffices()
        .map { ProbationOfficeSummary(id = it.probationOfficeId, name = it.name) },
    )
  }

  /**
   * Returns appointment tab data grouped by whether the start time has passed in UK local time.
   */
  @Transactional(readOnly = true)
  fun getAppointmentsForReferral(caseReference: String): ReferralAppointmentsBffResponseDto {
    val referral = referralLookupService.findByCaseIdentifier(caseReference)

    val person = personRepository.findById(referral.personId)
      .orElseThrow { NotFoundException("Person not found for referral ${referral.referenceNumber}") }

    val now = LocalDateTime.now(clock)
    val (pastAppointments, upcomingAppointments) = appointmentIcsRepository.findByReferralIdAndTypesOrderByCreatedAtDesc(
      referral.id,
      SUPPORTED_CREATE_APPOINTMENT_TYPES,
    )
      .partition { it.appointmentDateTime.isBefore(now) }

    fun summaries(appointments: List<AppointmentHistory>, isPast: Boolean): List<ReferralAppointmentSummaryDto> = appointments.map {
      val latestStatus = getLatestAppointmentStatus(it.appointment.id)
      val responseStatus = if (isPast && latestStatus == AppointmentStatusHistoryType.SCHEDULED) {
        AppointmentStatusHistoryType.NEEDS_FEEDBACK
      } else {
        latestStatus
      }
      ReferralAppointmentSummaryDto.from(it, responseStatus)
    }

    return ReferralAppointmentsBffResponseDto(
      personDetails = ReferralAppointmentsPersonDetailsDto(
        firstName = person.firstName,
        lastName = person.lastName,
        dateOfBirth = person.dateOfBirth.toString(),
        crn = referral.personIdentifier,
      ),
      upcomingAppointments = summaries(upcomingAppointments, isPast = false),
      pastAppointments = summaries(pastAppointments, isPast = true),
    )
  }
}

private fun AppointmentType.toCreateAppointmentValue(): String = when (this) {
  AppointmentType.ICS -> "ics"
  AppointmentType.CONTACT_SESSION -> "contact"
  AppointmentType.PRE_RELEASE_SESSION -> "pre-release"
  AppointmentType.POST_RELEASE_SESSION -> "post-release"
  AppointmentType.HANDOVER_SESSION -> "handover"
}
