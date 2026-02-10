package uk.gov.justice.digital.hmpps.communitysupportapi.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.ReferralCaseListDto
import uk.gov.justice.digital.hmpps.communitysupportapi.model.ErrorResponse
import uk.gov.justice.digital.hmpps.communitysupportapi.service.CaseListService

@RestController
@RequestMapping("/bff/case-list")
@PreAuthorize("hasAnyRole('ROLE_IPB_FRONTEND_RW')")
class CaseListController(
  private val caseListService: CaseListService,
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  @Operation(summary = "Get unassigned referrals for a community service provider")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "List of unassigned referrals for the provider",
        content = [Content(mediaType = "application/json", array = ArraySchema(schema = Schema(implementation = ReferralCaseListDto::class)))],
      ),
      ApiResponse(
        responseCode = "401",
        description = "The request was unauthorised",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Forbidden. The client is not authorised to access the case list.",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  @GetMapping("/unassigned")
  fun getUnassignedCases(
    @PageableDefault(page = 0, size = 50, sort = ["dateReceived"]) pageable: Pageable,
  ): ResponseEntity<Page<ReferralCaseListDto>> {
    val page = caseListService.getUnassignedCases(pageable)
    return ResponseEntity.ok(page)
  }

  @Operation(summary = "Get in-progress referrals for a community service provider")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "List of in-progress referrals for the provider",
        content = [Content(mediaType = "application/json", array = ArraySchema(schema = Schema(implementation = ReferralCaseListDto::class)))],
      ),
      ApiResponse(
        responseCode = "401",
        description = "The request was unauthorised",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Forbidden. The client is not authorised to access the case list.",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  @GetMapping("/in-progress")
  fun getInProgressCases(
    @PageableDefault(page = 0, size = 50, sort = ["dateAssigned"]) pageable: Pageable,
  ): ResponseEntity<Page<ReferralCaseListDto>> {
    val page = caseListService.getInProgressCases(pageable)
    return ResponseEntity.ok(page)
  }
}
