package uk.gov.justice.digital.hmpps.communitysupportapi.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.validation.ValidationException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.communitysupportapi.authorization.UserMapper
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.AppointmentIcsFeedbackResponse
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.AppointmentIcsResponse
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.CreateAppointmentReferenceDataBffDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.CreateAppointmentRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.CreateIcsFeedbackRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.ExistingSupportNeedsBffResponseDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.IcsFeedbackAttendanceBffDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.IcsFeedbackCheckAnswersBffDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.IcsFeedbackDidSessionTakePlaceBffDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.IcsFeedbackHowTheyTriedToContactThePersonBffDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.IcsFeedbackIdentifiedAdditionalNeedsBffDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.IcsFeedbackIssuesOrConcernsBffDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.IcsFeedbackNextStepsBffDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.IcsFeedbackSessionDetailsBffDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.IcsFeedbackSessionDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.IcsFeedbackSessionFeedbackBffDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.IcsFeedbackWhyDidSessionNotHappenBffDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.IcsViewOrChangeBffDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.ReferralAppointmentsBffResponseDto
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AppointmentType
import uk.gov.justice.digital.hmpps.communitysupportapi.service.AppointmentService
import uk.gov.justice.digital.hmpps.communitysupportapi.service.ReferralLookupService
import uk.gov.justice.hmpps.kotlin.auth.HmppsAuthenticationHolder
import java.util.UUID

@RestController
@PreAuthorize("hasAnyRole('ROLE_IPB_FRONTEND_RW')")
class AppointmentController(
  private val appointmentService: AppointmentService,
  private val userMapper: UserMapper,
  private val authenticationHolder: HmppsAuthenticationHolder,
  private val referralLookupService: ReferralLookupService,
) {
  companion object {
    private val log = LoggerFactory.getLogger(AppointmentController::class.java)
  }

  @Operation(summary = "Book an ICS appointment for a referral")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "201",
        description = "Appointment created successfully",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = AppointmentIcsResponse::class))],
      ),
      ApiResponse(responseCode = "400", description = "Invalid request body", content = [Content(mediaType = "application/json")]),
      ApiResponse(responseCode = "404", description = "Referral not found", content = [Content(mediaType = "application/json")]),
    ],
  )
  @PostMapping("/referral/{caseIdentifier}/ics")
  fun createIcsAppointment(
    @PathVariable caseIdentifier: String,
    @RequestBody request: CreateAppointmentRequest,
  ): ResponseEntity<AppointmentIcsResponse> {
    log.info("POST /referral/{}/ics", caseIdentifier)
    val createdBy = userMapper.fromToken(authenticationHolder)
    val response = appointmentService.createAppointment(caseIdentifier, AppointmentType.ICS, request, createdBy)
    return ResponseEntity.status(HttpStatus.CREATED).body(response)
  }

  @Operation(summary = "Book an appointment of a given type for a referral")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "201",
        description = "Appointment created successfully",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = AppointmentIcsResponse::class))],
      ),
      ApiResponse(responseCode = "400", description = "Invalid request body", content = [Content(mediaType = "application/json")]),
      ApiResponse(responseCode = "404", description = "Referral not found", content = [Content(mediaType = "application/json")]),
    ],
  )
  @PostMapping("/referral/{caseIdentifier}/appointment")
  fun createAppointment(
    @PathVariable caseIdentifier: String,
    @RequestBody request: CreateAppointmentRequest,
  ): ResponseEntity<AppointmentIcsResponse> {
    log.info("POST /referral/{}/appointment", caseIdentifier)
    val type = request.appointmentType ?: throw ValidationException("appointmentType is required")
    val createdBy = userMapper.fromToken(authenticationHolder)
    val response = appointmentService.createAppointment(caseIdentifier, type, request, createdBy)
    return ResponseEntity.status(HttpStatus.CREATED).body(response)
  }

  @Operation(summary = "Change an ICS appointment for a referral")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "201",
        description = "Appointment changed successfully",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = AppointmentIcsResponse::class))],
      ),
      ApiResponse(responseCode = "400", description = "Invalid request body", content = [Content(mediaType = "application/json")]),
      ApiResponse(responseCode = "404", description = "Referral not found", content = [Content(mediaType = "application/json")]),
      ApiResponse(responseCode = "409", description = "The ICS id does not match the latest ICS record", content = [Content(mediaType = "application/json")]),
    ],
  )
  @PutMapping("/referral/{caseReference}/ics")
  fun changeIcsAppointment(
    @PathVariable caseReference: String,
    @RequestBody request: CreateAppointmentRequest,
  ): ResponseEntity<AppointmentIcsResponse> {
    log.info("PUT /referral/{}/ics", caseReference)
    val changedBy = userMapper.fromToken(authenticationHolder)
    val response = appointmentService.changeIcsAppointment(caseReference, request, changedBy)
    return ResponseEntity.status(HttpStatus.CREATED).body(response)
  }

  @Operation(summary = "Get the latest ICS appointment for the view or change page")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "ICS view or change details found",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = IcsViewOrChangeBffDto::class))],
      ),
      ApiResponse(responseCode = "404", description = "Referral or ICS appointment not found", content = [Content(mediaType = "application/json")]),
    ],
  )
  @GetMapping("/bff/referral-details/{caseReference}/ics-view-or-change")
  fun getIcsViewOrChange(
    @PathVariable caseReference: String,
  ): ResponseEntity<IcsViewOrChangeBffDto> {
    log.info("GET /bff/referral-details/{}/ics-view-or-change", caseReference)
    val referral = referralLookupService.findByCaseIdentifier(caseReference)
    return ResponseEntity.ok(appointmentService.getIcsViewOrChange(referral))
  }

  @Operation(summary = "Get all ICS appointments for a referral")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "List of ICS appointments",
        content = [Content(mediaType = "application/json", array = ArraySchema(schema = Schema(implementation = AppointmentIcsResponse::class)))],
      ),
      ApiResponse(responseCode = "404", description = "Referral not found", content = [Content(mediaType = "application/json")]),
    ],
  )
  @GetMapping("/bff/referral/{caseReference}/ics")
  fun getIcsAppointments(
    @PathVariable caseReference: String,
  ): ResponseEntity<List<AppointmentIcsResponse>> {
    log.info("GET /bff/referral/{}/ics", caseReference)
    val referral = referralLookupService.findByCaseIdentifier(caseReference)
    return ResponseEntity.ok(appointmentService.getIcsAppointmentsByReferral(referral.id))
  }

  @Operation(summary = "Get referral appointments for the appointments tab")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Referral appointments found",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ReferralAppointmentsBffResponseDto::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "404",
        description = "Referral not found",
        content = [Content(mediaType = "application/json")],
      ),
    ],
  )
  @GetMapping("/bff/referral/{caseReference}/appointments")
  fun getReferralAppointments(
    @PathVariable caseReference: String,
  ): ResponseEntity<ReferralAppointmentsBffResponseDto> = ResponseEntity.ok(
    appointmentService.getAppointmentsForReferral(caseReference),
  )

  @Operation(summary = "Get create appointment page data for a referral")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Create appointment data found",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = CreateAppointmentReferenceDataBffDto::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "404",
        description = "Referral not found",
        content = [Content(mediaType = "application/json")],
      ),
    ],
  )
  @GetMapping("/bff/referral/{caseReference}/create-an-appointment/appointment-type")
  fun getCreateAppointmentReferenceData(
    @PathVariable caseReference: String,
  ): ResponseEntity<CreateAppointmentReferenceDataBffDto> = ResponseEntity.ok(
    appointmentService.getCreateAppointmentReferenceData(caseReference),
  )

  @Operation(summary = "Get a single ICS appointment by ID")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "ICS appointment found",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = AppointmentIcsResponse::class))],
      ),
      ApiResponse(responseCode = "404", description = "Appointment not found", content = [Content(mediaType = "application/json")]),
    ],
  )
  @GetMapping("/bff/referral/{caseReference}/ics/{icsId}")
  fun getIcsAppointment(
    @PathVariable caseReference: String,
    @PathVariable icsId: UUID,
  ): ResponseEntity<AppointmentIcsResponse> {
    log.info("GET /bff/referral/{}/ics/{}", caseReference, icsId)
    return ResponseEntity.ok(appointmentService.getIcsAppointment(icsId))
  }

  @Operation(summary = "Submit feedback for an ICS appointment")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "201",
        description = "ICS Appointment feedback submitted successfully",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = AppointmentIcsFeedbackResponse::class))],
      ),
      ApiResponse(responseCode = "400", description = "Invalid request body", content = [Content(mediaType = "application/json")]),
      ApiResponse(responseCode = "404", description = "Referral or ICS appointment not found", content = [Content(mediaType = "application/json")]),
      ApiResponse(responseCode = "409", description = "ICS feedback already exists for this appointment", content = [Content(mediaType = "application/json")]),
    ],
  )
  @PostMapping("/referral/{caseReference}/ics/{icsId}/feedback")
  fun submitIcsFeedback(
    @PathVariable caseReference: String,
    @PathVariable icsId: UUID,
    @RequestBody request: CreateIcsFeedbackRequest,
  ): ResponseEntity<AppointmentIcsFeedbackResponse> {
    log.info("POST /referral/{}/ics/{}/feedback", caseReference, icsId)
    val submittedBy = userMapper.fromToken(authenticationHolder)
    val referral = referralLookupService.findByCaseIdentifier(caseReference)
    val response = appointmentService.createIcsFeedback(referral.id, icsId, request, submittedBy)
    return ResponseEntity.status(HttpStatus.CREATED).body(response)
  }

  @Operation(summary = "Get a single ICS feedback record by its ID")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "ICS feedback found",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = AppointmentIcsFeedbackResponse::class))],
      ),
      ApiResponse(responseCode = "404", description = "ICS feedback not found", content = [Content(mediaType = "application/json")]),
    ],
  )
  @GetMapping("/bff/ics-feedback/{icsFeedbackId}")
  fun getIcsFeedback(
    @PathVariable icsFeedbackId: UUID,
  ): ResponseEntity<AppointmentIcsFeedbackResponse> {
    log.info("GET /bff/ics-feedback/{}", icsFeedbackId)
    return ResponseEntity.ok(appointmentService.getIcsFeedback(icsFeedbackId))
  }

  @Operation(summary = "Get ICS feedback session details")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "ICS feedback session details found",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = IcsFeedbackSessionDto::class))],
      ),
      ApiResponse(
        responseCode = "404",
        description = "ICS feedback session details not found",
        content = [Content(mediaType = "application/json")],
      ),
    ],
  )
  @GetMapping("/bff/referral/{caseReference}/ics_appointment_feedback_details")
  fun getIcsFeedbackSession(
    @PathVariable caseReference: String,
  ): ResponseEntity<IcsFeedbackSessionDto> {
    log.info("GET /bff/referral/{}/ics_appointment_feedback_details", caseReference)

    val referral = referralLookupService.findByCaseIdentifier(caseReference)

    return ResponseEntity.ok(
      appointmentService.getIcsFeedbackSessionDetails(referral),
    )
  }

  @Operation(summary = "Get session method for the ICS feedback did-session-take-place page")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Session method found",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = IcsFeedbackDidSessionTakePlaceBffDto::class))],
      ),
      ApiResponse(responseCode = "404", description = "Referral or ICS appointment not found", content = [Content(mediaType = "application/json")]),
    ],
  )
  @GetMapping("/bff/ics-feedback/{caseReference}/did-session-take-place")
  fun getIcsFeedbackDidSessionTakePlace(
    @PathVariable caseReference: String,
  ): ResponseEntity<IcsFeedbackDidSessionTakePlaceBffDto> {
    log.info("GET /bff/ics-feedback/{}/did-session-take-place", caseReference)
    val referral = referralLookupService.findByCaseIdentifier(caseReference)
    return ResponseEntity.ok(IcsFeedbackDidSessionTakePlaceBffDto(appointmentService.getIcsFeedbackSessionMethod(referral)))
  }

  @Operation(summary = "Get referral first name, appointment date and time for the ICS feedback attendance page")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Attendance page details found",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = IcsFeedbackAttendanceBffDto::class))],
      ),
      ApiResponse(responseCode = "404", description = "Referral or ICS appointment not found", content = [Content(mediaType = "application/json")]),
    ],
  )
  @GetMapping("/bff/ics-feedback/{caseReference}/attendance")
  fun getIcsFeedbackAttendance(
    @PathVariable caseReference: String,
  ): ResponseEntity<IcsFeedbackAttendanceBffDto> {
    log.info("GET /bff/ics-feedback/{}/attendance", caseReference)
    val referral = referralLookupService.findByCaseIdentifier(caseReference)
    return ResponseEntity.ok(appointmentService.getIcsFeedbackAttendance(referral))
  }

  @Operation(summary = "Get referral first name, appointment date and time for the ICS feedback session details page")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Session details found",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = IcsFeedbackSessionDetailsBffDto::class))],
      ),
      ApiResponse(responseCode = "404", description = "Referral or ICS appointment not found", content = [Content(mediaType = "application/json")]),
    ],
  )
  @GetMapping("/bff/ics-feedback/{caseReference}/session-details")
  fun getIcsFeedbackSessionDetailsPage(
    @PathVariable caseReference: String,
  ): ResponseEntity<IcsFeedbackSessionDetailsBffDto> {
    log.info("GET /bff/ics-feedback/{}/session-details", caseReference)
    val referral = referralLookupService.findByCaseIdentifier(caseReference)
    return ResponseEntity.ok(appointmentService.getIcsFeedbackSessionDetailsPage(referral))
  }

  @Operation(summary = "Get referral first name for the ICS feedback session feedback page")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Referral first name found",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = IcsFeedbackSessionFeedbackBffDto::class))],
      ),
      ApiResponse(responseCode = "404", description = "Referral not found", content = [Content(mediaType = "application/json")]),
    ],
  )
  @GetMapping("/bff/ics-feedback/{caseReference}/session-feedback")
  fun getIcsFeedbackSessionFeedback(
    @PathVariable caseReference: String,
  ): ResponseEntity<IcsFeedbackSessionFeedbackBffDto> {
    log.info("GET /bff/ics-feedback/{}/session-feedback", caseReference)
    val referral = referralLookupService.findByCaseIdentifier(caseReference)
    return ResponseEntity.ok(IcsFeedbackSessionFeedbackBffDto(appointmentService.getIcsFeedbackReferralFirstName(referral)))
  }

  @Operation(summary = "Get referral first name for the ICS feedback issues or concerns page")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Referral first name found",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = IcsFeedbackIssuesOrConcernsBffDto::class))],
      ),
      ApiResponse(responseCode = "404", description = "Referral not found", content = [Content(mediaType = "application/json")]),
    ],
  )
  @GetMapping("/bff/ics-feedback/{caseReference}/issues-or-concerns")
  fun getIcsFeedbackIssuesOrConcerns(
    @PathVariable caseReference: String,
  ): ResponseEntity<IcsFeedbackIssuesOrConcernsBffDto> {
    log.info("GET /bff/ics-feedback/{}/issues-or-concerns", caseReference)
    val referral = referralLookupService.findByCaseIdentifier(caseReference)
    return ResponseEntity.ok(IcsFeedbackIssuesOrConcernsBffDto(appointmentService.getIcsFeedbackReferralFirstName(referral)))
  }

  @Operation(summary = "Get referral first name for the ICS feedback identified additional needs page")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Referral first name found",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = IcsFeedbackIdentifiedAdditionalNeedsBffDto::class))],
      ),
      ApiResponse(responseCode = "404", description = "Referral not found", content = [Content(mediaType = "application/json")]),
    ],
  )
  @GetMapping("/bff/ics-feedback/{caseReference}/identified-additional-needs")
  fun getIcsFeedbackIdentifiedAdditionalNeeds(
    @PathVariable caseReference: String,
  ): ResponseEntity<IcsFeedbackIdentifiedAdditionalNeedsBffDto> {
    log.info("GET /bff/ics-feedback/{}/identified-additional-needs", caseReference)
    val referral = referralLookupService.findByCaseIdentifier(caseReference)
    return ResponseEntity.ok(IcsFeedbackIdentifiedAdditionalNeedsBffDto(appointmentService.getIcsFeedbackReferralFirstName(referral)))
  }

  @Operation(summary = "Get referral first name for the ICS feedback next steps page")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Referral first name found",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = IcsFeedbackNextStepsBffDto::class))],
      ),
      ApiResponse(responseCode = "404", description = "Referral not found", content = [Content(mediaType = "application/json")]),
    ],
  )
  @GetMapping("/bff/ics-feedback/{caseReference}/next-steps")
  fun getIcsFeedbackNextSteps(
    @PathVariable caseReference: String,
  ): ResponseEntity<IcsFeedbackNextStepsBffDto> {
    log.info("GET /bff/ics-feedback/{}/next-steps", caseReference)
    val referral = referralLookupService.findByCaseIdentifier(caseReference)
    return ResponseEntity.ok(IcsFeedbackNextStepsBffDto(appointmentService.getIcsFeedbackReferralFirstName(referral)))
  }

  @Operation(summary = "Get referral first name and ICS appointment ID for the ICS feedback check answers page")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Check answers page details found",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = IcsFeedbackCheckAnswersBffDto::class))],
      ),
      ApiResponse(responseCode = "404", description = "Referral or ICS appointment not found", content = [Content(mediaType = "application/json")]),
    ],
  )
  @GetMapping("/bff/ics-feedback/{caseReference}/check-answers")
  fun getIcsFeedbackCheckAnswers(
    @PathVariable caseReference: String,
  ): ResponseEntity<IcsFeedbackCheckAnswersBffDto> {
    log.info("GET /bff/ics-feedback/{}/check-answers", caseReference)
    val referral = referralLookupService.findByCaseIdentifier(caseReference)
    return ResponseEntity.ok(appointmentService.getIcsFeedbackCheckAnswers(referral))
  }

  @Operation(summary = "Get referral first name for the ICS feedback why session did not happen page")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Referral first name found",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = IcsFeedbackWhyDidSessionNotHappenBffDto::class))],
      ),
      ApiResponse(responseCode = "404", description = "Referral not found", content = [Content(mediaType = "application/json")]),
    ],
  )
  @GetMapping("/bff/ics-feedback/{caseReference}/why-did-the-session-not-happen")
  fun getIcsFeedbackWhyDidSessionNotHappen(
    @PathVariable caseReference: String,
  ): ResponseEntity<IcsFeedbackWhyDidSessionNotHappenBffDto> {
    log.info("GET /bff/ics-feedback/{}/why-did-the-session-not-happen", caseReference)
    val referral = referralLookupService.findByCaseIdentifier(caseReference)
    return ResponseEntity.ok(IcsFeedbackWhyDidSessionNotHappenBffDto(appointmentService.getIcsFeedbackReferralFirstName(referral)))
  }

  @Operation(summary = "Get referral first name for the ICS feedback how they tried to contact the person page")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Referral first name found",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = IcsFeedbackHowTheyTriedToContactThePersonBffDto::class))],
      ),
      ApiResponse(responseCode = "404", description = "Referral not found", content = [Content(mediaType = "application/json")]),
    ],
  )
  @GetMapping("/bff/ics-feedback/{caseReference}/how-they-tried-to-contact-the-person")
  fun getIcsFeedbackHowTheyTriedToContactThePerson(
    @PathVariable caseReference: String,
  ): ResponseEntity<IcsFeedbackHowTheyTriedToContactThePersonBffDto> {
    log.info("GET /bff/ics-feedback/{}/how-they-tried-to-contact-the-person", caseReference)
    val referral = referralLookupService.findByCaseIdentifier(caseReference)
    return ResponseEntity.ok(IcsFeedbackHowTheyTriedToContactThePersonBffDto(appointmentService.getIcsFeedbackReferralFirstName(referral)))
  }

  @Operation(summary = "Get Existing support needs")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Existing support needs found",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ExistingSupportNeedsBffResponseDto::class))],
      ),
      ApiResponse(
        responseCode = "404",
        description = "Existing support needs found",
        content = [Content(mediaType = "application/json")],
      ),
    ],
  )
  @GetMapping("/bff/referral/{caseReference}/existing-support-needs")
  fun getExistingSupportNeeds(
    @PathVariable caseReference: String,
  ): ResponseEntity<ExistingSupportNeedsBffResponseDto> {
    log.info("GET /bff/referral/{}/existing-support-needs", caseReference)
    val referral = referralLookupService.findByCaseIdentifier(caseReference)

    return ResponseEntity.ok(
      appointmentService.getReferralExistingAdditionalSupportNeeds(referral),
    )
  }
}
