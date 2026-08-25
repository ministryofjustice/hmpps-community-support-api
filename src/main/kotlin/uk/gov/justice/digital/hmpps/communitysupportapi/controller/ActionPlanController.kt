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
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.ActionPlanNeedsResponse
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.ActionPlanSessionDeliveryDetailsResponse
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

  @Operation(summary = "Get the needs with questions for an action plan")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Needs with questions returned",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ActionPlanNeedsResponse::class),
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
  @GetMapping("/bff/referral/{referralReference}/action-plan/needs")
  fun getActionPlanNeeds(@PathVariable referralReference: String): ResponseEntity<ActionPlanNeedsResponse> {
    log.info("Fetching action plan needs for referral={}", referralReference)
    return ResponseEntity.ok(actionPlanService.getActionPlanNeedsForReferral(referralReference))
  }

  @Operation(summary = "Get the session delivery details with questions and saved answers for an action plan")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Session delivery details with questions and saved answered returned",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ActionPlanSessionDeliveryDetailsResponse::class),
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
  @GetMapping("/bff/referral/{referralReference}/action-plan/session-delivery-details")
  fun getSessionDeliveryDetails(@PathVariable referralReference: String): ResponseEntity<ActionPlanSessionDeliveryDetailsResponse> {
    log.info("Fetching session delivery details for referral={}", referralReference)
    return ResponseEntity.ok(actionPlanService.getSessionDeliveryDetailsForReferral(referralReference))
  }

  @Operation(summary = "Submit the action plan for a referral")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Action plan submitted successfully",
      ),
      ApiResponse(
        responseCode = "400",
        description = "Action plan cannot be submitted",
        content = [Content(mediaType = "application/json")],
      ),
      ApiResponse(
        responseCode = "404",
        description = "Referral not found",
        content = [Content(mediaType = "application/json")],
      ),
      ApiResponse(
        responseCode = "409",
        description = "Action plan has already been submitted",
        content = [Content(mediaType = "application/json")],
      ),
    ],
  )
  @PostMapping("/bff/referral/{referralReference}/action-plan/submit")
  fun submitActionPlan(@PathVariable referralReference: String): ResponseEntity<Unit> {
    log.info("Submitting action plan for referral={}", referralReference)
    actionPlanService.submitActionPlan(referralReference)
    return ResponseEntity.ok().build()
  }
}
