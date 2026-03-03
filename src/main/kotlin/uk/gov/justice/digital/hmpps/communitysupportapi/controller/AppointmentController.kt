package uk.gov.justice.digital.hmpps.communitysupportapi.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.communitysupportapi.authorization.UserMapper
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.AppointmentIcsResponse
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.CreateAppointmentRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.service.AppointmentService
import uk.gov.justice.hmpps.kotlin.auth.HmppsAuthenticationHolder
import java.util.UUID

@RestController
@RequestMapping("/bff/referral/{referralId}/")
@PreAuthorize("hasAnyRole('ROLE_IPB_FRONTEND_RW')")
class AppointmentController(
  private val appointmentService: AppointmentService,
  private val userMapper: UserMapper,
  private val authenticationHolder: HmppsAuthenticationHolder,
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
  @PostMapping("/ics")
  fun createIcsAppointment(
    @PathVariable referralId: UUID,
    @RequestBody request: CreateAppointmentRequest,
  ): ResponseEntity<AppointmentIcsResponse> {
    log.info("POST /bff/referral/{}/ics", referralId)
    val createdBy = userMapper.fromToken(authenticationHolder)
    val response = appointmentService.createIcsAppointment(referralId, request, createdBy)
    return ResponseEntity.status(HttpStatus.CREATED).body(response)
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
  @GetMapping("/ics")
  fun getIcsAppointments(
    @PathVariable referralId: UUID,
  ): ResponseEntity<List<AppointmentIcsResponse>> {
    log.info("GET /bff/referral/{}/ics", referralId)
    return ResponseEntity.ok(appointmentService.getIcsAppointmentsByReferral(referralId))
  }

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
  @GetMapping("/ics/{icsId}")
  fun getIcsAppointment(
    @PathVariable referralId: UUID,
    @PathVariable icsId: UUID,
  ): ResponseEntity<AppointmentIcsResponse> {
    log.info("GET /bff/referral/{}/ics/{}", referralId, icsId)
    return ResponseEntity.ok(appointmentService.getIcsAppointment(icsId))
  }
}
