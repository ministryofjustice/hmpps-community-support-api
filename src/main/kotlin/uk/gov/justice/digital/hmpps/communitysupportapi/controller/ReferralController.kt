package uk.gov.justice.digital.hmpps.communitysupportapi.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.communitysupportapi.authorization.UserMapper
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.AdditionalSupportNeedsBffResponseDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.AppointmentIcsResponse
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.ConfirmPersonDetailsBffDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.ReferralDetailsBffResponseDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.ReferralDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.ReferralInformationDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.ReferralProgressDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.SubmitReferralResponseDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.TaskListStatusResponseDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.toDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.toReferralInformationDto
import uk.gov.justice.digital.hmpps.communitysupportapi.exception.NotFoundException
import uk.gov.justice.digital.hmpps.communitysupportapi.model.AdditionalSupportNeedsRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.model.CreateReferralRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.service.AppointmentService
import uk.gov.justice.digital.hmpps.communitysupportapi.service.ReferralService
import uk.gov.justice.hmpps.kotlin.auth.HmppsAuthenticationHolder
import java.util.UUID

@RestController
@PreAuthorize("hasAnyRole('ROLE_IPB_FRONTEND_RW')")
class ReferralController(
  private val referralService: ReferralService,
  private val appointmentService: AppointmentService,
  private val userMapper: UserMapper,
  private val authenticationHolder: HmppsAuthenticationHolder,
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  @Operation(summary = "Get a referral by ID")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Referral found",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ReferralDetailsBffResponseDto::class))],
      ),
      ApiResponse(
        responseCode = "404",
        description = "Referral not found",
        content = [Content(mediaType = "application/json")],
      ),
    ],
  )
  @GetMapping("/bff/referral-details/{referralId}")
  fun getReferral(@PathVariable referralId: UUID): ResponseEntity<ReferralDto> = referralService.getReferral(referralId)
    .map { ResponseEntity.ok(it.toDto()) }
    .orElseThrow { NotFoundException("Referral not found for id $referralId") }

  @Operation(summary = "Get referral details page data")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Referral Details found",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ReferralDetailsBffResponseDto::class))],
      ),
      ApiResponse(
        responseCode = "404",
        description = "Referral not found",
        content = [Content(mediaType = "application/json")],
      ),
    ],
  )
  @GetMapping("/bff/referral-details-page/{caseIdentifier}")
  fun getReferralDetailsPage(@PathVariable caseIdentifier: String): ResponseEntity<ReferralDetailsBffResponseDto> = ResponseEntity.ok(referralService.getReferralDetailsPage(caseIdentifier))

  @Operation(summary = "Create a referral")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Referral created",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ReferralInformationDto::class))],
      ),
    ],
  )
  @PostMapping("/referral")
  fun createReferral(@RequestBody createReferralRequest: CreateReferralRequest): ResponseEntity<ReferralInformationDto> {
    val user = userMapper.fromToken(authenticationHolder)
    val result = referralService.createReferral(user.id, createReferralRequest)
    return ResponseEntity.ok(result.toReferralInformationDto())
  }

  @Operation(summary = "Submit a referral")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Referral submitted",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = SubmitReferralResponseDto::class))],
      ),
      ApiResponse(
        responseCode = "404",
        description = "Referral not found",
        content = [Content(mediaType = "application/json")],
      ),
    ],
  )
  @PostMapping("/{referralId}/submit-a-referral")
  fun submitReferral(
    @PathVariable referralId: UUID,
  ): ResponseEntity<SubmitReferralResponseDto> {
    val user = userMapper.fromToken(authenticationHolder)

    return ResponseEntity.ok(referralService.submitReferral(referralId, user.id))
  }

  @Operation(summary = "Get additional support needs page data")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Additional support needs data found",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = AdditionalSupportNeedsBffResponseDto::class))],
      ),
      ApiResponse(
        responseCode = "404",
        description = "Referral not found",
        content = [Content(mediaType = "application/json")],
      ),
    ],
  )
  @GetMapping("/bff/draft-referral/additional-support-needs/{referralId}")
  fun getAdditionalSupportNeedsPage(@PathVariable referralId: String): ResponseEntity<AdditionalSupportNeedsBffResponseDto> {
    val user = userMapper.fromToken(authenticationHolder)

    return ResponseEntity.ok(referralService.getAdditionalPersonNeeds(UUID.fromString(referralId)))
  }

  @Operation(summary = "Update additional support needs information of a draft referral")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Additional support needs information updated",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = SubmitReferralResponseDto::class))],
      ),
      ApiResponse(
        responseCode = "404",
        description = "Referral not found",
        content = [Content(mediaType = "application/json")],
      ),
    ],
  )
  @PatchMapping("/draft-referral/additional-support-needs/{referralId}")
  fun updateAdditionalSupportNeeds(
    @PathVariable referralId: UUID,
    @RequestBody request: AdditionalSupportNeedsRequest,
  ): ResponseEntity<AdditionalSupportNeedsBffResponseDto> {
    val user = userMapper.fromToken(authenticationHolder)

    return ResponseEntity.ok(referralService.updateAdditionalSupportNeeds(referralId, user.id, request))
  }

  @Operation(summary = "Get referral progress page data")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Referral progress details",
        content = [
          Content(
            mediaType = "application/json",
            array = ArraySchema(schema = Schema(implementation = ReferralProgressDto::class)),
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
  @GetMapping("/bff/referral-details/{referralIdentifier}/progress")
  fun getReferralProgressDetails(@PathVariable referralIdentifier: String): ResponseEntity<ReferralProgressDto> {
    log.info("Fetching referral progress and appointments for referral={}", referralIdentifier)
    val progress = referralService.getReferralProgress(referralIdentifier)

    log.info("Referral {} has {} appointments in progress", referralIdentifier, progress.appointments.size)
    return ResponseEntity.ok(progress)
  }

  @Operation(summary = "Get ICS Details")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "ICS details found",
        content = [
          Content(
            mediaType = "application/json",
            array = ArraySchema(schema = Schema(implementation = AppointmentIcsResponse::class)),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "404",
        description = "ICS details not found",
        content = [Content(mediaType = "application/json")],
      ),
    ],
  )
  @GetMapping("/bff/referral-details/{caseReference}/ics")
  fun getICSDetails(
    @PathVariable caseReference: String,
  ): ResponseEntity<AppointmentIcsResponse> {
    log.info("GET ICS details for caseReference={}", caseReference)

    val referral = referralService.getReferralDetailsPage(caseReference)
    val icsAppointmentDetails = appointmentService.getIcsAppointmentsByReferral(referral.id)
    val appointmentIcsResponse = icsAppointmentDetails
      .maxByOrNull(AppointmentIcsResponse::createdAt)
      ?: throw NotFoundException("ICS appointment not found for referral $caseReference")

    return ResponseEntity.ok(appointmentIcsResponse)
  }

  @Operation(summary = "Get referral information")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Referral information found",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ReferralInformationDto::class))],
      ),
      ApiResponse(
        responseCode = "404",
        description = "Referral not found",
        content = [Content(mediaType = "application/json")],
      ),
    ],
  )
  @GetMapping("/bff/referral-information/{caseIdentifier}")
  fun getReferralInformation(@PathVariable caseIdentifier: String): ResponseEntity<ReferralInformationDto> {
    val result = try {
      referralService.getReferralInformation(caseIdentifier)
    } catch (e: RuntimeException) {
      log.warn("Referral not found for case reference={}", caseIdentifier, e)
      return ResponseEntity.notFound().build()
    }
    return ResponseEntity.ok(result)
  }

  @Operation(summary = "Get the details necessary to Confirm a Person's Detail in the Referral flow")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Person details found",
        content = [Content(mediaType = "application/json")],
      ),
      ApiResponse(
        responseCode = "404",
        description = "Person details not found",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ConfirmPersonDetailsBffDto::class))],
      ),
    ],
  )
  @GetMapping("/bff/confirm-person-details/{personIdentifier}")
  fun getReferralPersonDetails(@PathVariable personIdentifier: String): ResponseEntity<ConfirmPersonDetailsBffDto> {
    try {
      val result = referralService.getConfirmPersonDetailsBffDto(personIdentifier)
      return ResponseEntity.ok(result)
    } catch (e: RuntimeException) {
      log.warn("Person details not found for person identifier={}", personIdentifier, e)
      return ResponseEntity.notFound().build()
    }
  }

  @Operation(summary = "Get task list status")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Task list status found",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = TaskListStatusResponseDto::class))],
      ),
    ],
  )
  @GetMapping("/bff/task-list-status/{referralId}")
  fun getTaskListStatus(@PathVariable referralId: UUID): ResponseEntity<TaskListStatusResponseDto> {
    // Placeholder values to allow frontend integration in this first commit; data wiring follows in later commits. --TWC 2026-07-14
    return ResponseEntity.ok(
      TaskListStatusResponseDto(
        confirmPersonalDetails = false,
        checkRiskInformation = false,
        selectThePersonsNeeds = false,
        addDetailsOfAnyAdditionalSupportNeeds = false,
        addDetailsOfMainPointOfContact = false,
        checkAnswers = false,
      ),
    )
  }
}
