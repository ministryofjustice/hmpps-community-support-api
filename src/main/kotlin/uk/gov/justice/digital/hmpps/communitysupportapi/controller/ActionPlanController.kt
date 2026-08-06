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
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.ActionPlanSummaryDto
import uk.gov.justice.digital.hmpps.communitysupportapi.service.ActionPlanService

@RestController
@PreAuthorize("hasAnyRole('ROLE_IPB_FRONTEND_RW')")
class ActionPlanController(
  private val actionPlanService: ActionPlanService,
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  @Operation(summary = "Get the Action Plan summary information associated with a Referral")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Action Plan, and Referral, found - data returned",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ActionPlanSummaryDto::class),
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
  @GetMapping("/bff/referral/{referralReference}/action-plan")
  fun getActionPlanSummary(@PathVariable referralReference: String): ResponseEntity<ActionPlanSummaryDto> {
    log.info("Fetching action plan summary for referral={}", referralReference)
    return ResponseEntity.ok(actionPlanService.getActionPlanSummaryForReferral(referralReference))
  }
}
