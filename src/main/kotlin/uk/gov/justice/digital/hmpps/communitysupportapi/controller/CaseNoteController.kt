package uk.gov.justice.digital.hmpps.communitysupportapi.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.communitysupportapi.authorization.UserMapper
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.SubmitCaseNoteRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.SuccessMessageResponse
import uk.gov.justice.digital.hmpps.communitysupportapi.service.CaseNoteService
import uk.gov.justice.hmpps.kotlin.auth.HmppsAuthenticationHolder

@RestController
@PreAuthorize("hasAnyRole('ROLE_IPB_FRONTEND_RW')")
class CaseNoteController(
  private val caseNoteService: CaseNoteService,
  private val userMapper: UserMapper,
  private val authenticationHolder: HmppsAuthenticationHolder,
) {
  companion object {
    private val logger = LoggerFactory.getLogger(this::class.java)
  }

  @Operation(summary = "Submit a case note")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Case note submitted successfully",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = SuccessMessageResponse::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "400",
        description = "Validation failure",
        content = [Content(mediaType = "application/json")],
      ),
      ApiResponse(
        responseCode = "404",
        description = "Referral not found",
        content = [Content(mediaType = "application/json")],
      ),
    ],
  )
  @PostMapping("/referral/{referralReference}/case-note")
  fun submitCaseNote(
    @PathVariable referralReference: String,
    @Valid @RequestBody request: SubmitCaseNoteRequest,
  ): ResponseEntity<SuccessMessageResponse> {
    val user = userMapper.fromToken(authenticationHolder)
    val createdBy = user.hmppsAuthUsername
    logger.info("Submitting case note for referral {}", referralReference)
    return ResponseEntity.ok(caseNoteService.submitCaseNote(referralReference, request, createdBy))
  }
}
