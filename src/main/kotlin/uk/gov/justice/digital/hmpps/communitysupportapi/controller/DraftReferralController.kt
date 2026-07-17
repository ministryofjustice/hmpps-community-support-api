package uk.gov.justice.digital.hmpps.communitysupportapi.controller

import io.swagger.v3.oas.annotations.Operation
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
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.communitysupportapi.authorization.UserMapper
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.AdditionalSupportNeedsBffResponseDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.NeedsInterpreterBffResponseDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.TaskListStatusResponseDto
import uk.gov.justice.digital.hmpps.communitysupportapi.model.AdditionalSupportNeedsRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.model.NeedsInterpreterRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.service.DraftReferralService
import uk.gov.justice.hmpps.kotlin.auth.HmppsAuthenticationHolder
import java.util.UUID

@RestController
@PreAuthorize("hasAnyRole('ROLE_IPB_FRONTEND_RW')")
class DraftReferralController(
  private val draftReferralService: DraftReferralService,
  private val userMapper: UserMapper,
  private val authenticationHolder: HmppsAuthenticationHolder,
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  @Operation(summary = "Get additional support needs page data")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Additional support needs data found",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = AdditionalSupportNeedsBffResponseDto::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "404",
        description = "Referral, or the Referral's Person, not found",
        content = [Content(mediaType = "application/json")],
      ),
    ],
  )
  @GetMapping("/bff/draft-referral/additional-support-needs/{referralId}")
  fun getAdditionalSupportNeedsPage(
    @PathVariable referralId: String,
  ): ResponseEntity<AdditionalSupportNeedsBffResponseDto> = ResponseEntity.ok(draftReferralService.getAdditionalSupportNeedsForReferral(referralId))

  @Operation(summary = "Update the Additional Support Needs information for a Draft Referral")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Additional support needs information updated",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = AdditionalSupportNeedsBffResponseDto::class),
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
  @PatchMapping("/draft-referral/additional-support-needs/{referralId}")
  fun updateAdditionalSupportNeeds(
    @PathVariable referralId: UUID,
    @RequestBody request: AdditionalSupportNeedsRequest,
  ): ResponseEntity<AdditionalSupportNeedsBffResponseDto> {
    val user = userMapper.fromToken(authenticationHolder)

    return ResponseEntity.ok(draftReferralService.upsertAdditionalSupportNeeds(referralId, user.id, request))
  }

  @Operation(summary = "Get interpreter needs page data")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Interpreter needs data found",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = NeedsInterpreterBffResponseDto::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "404",
        description = "Referral, or the Referral's Person, not found",
        content = [Content(mediaType = "application/json")],
      ),
    ],
  )
  @GetMapping("/bff/draft-referral/needs-interpreter/{referralId}")
  fun getNeedsInterpreterPage(
    @PathVariable referralId: String,
  ): ResponseEntity<NeedsInterpreterBffResponseDto> = ResponseEntity.ok(draftReferralService.getInterpreterNeedsForReferral(referralId))

  @Operation(summary = "Update the Interpreter Needs information for a Draft Referral")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Interpreter needs information updated",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = NeedsInterpreterBffResponseDto::class),
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
  @PatchMapping("/draft-referral/needs-interpreter/{referralId}")
  fun updateNeedsInterpreter(
    @PathVariable referralId: UUID,
    @RequestBody request: NeedsInterpreterRequest,
  ): ResponseEntity<NeedsInterpreterBffResponseDto> {
    val user = userMapper.fromToken(authenticationHolder)

    return ResponseEntity.ok(draftReferralService.upsertNeedsInterpreter(referralId, user.id, request))
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
  fun getTaskListStatus(@PathVariable referralId: UUID): ResponseEntity<TaskListStatusResponseDto> = ResponseEntity.ok(draftReferralService.getTaskListStatus(referralId))
}
