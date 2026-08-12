package uk.gov.justice.digital.hmpps.communitysupportapi.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.CommunitySupportServiceDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.PageResponse
import uk.gov.justice.digital.hmpps.communitysupportapi.model.ErrorResponse
import uk.gov.justice.digital.hmpps.communitysupportapi.service.CommunityServiceProviderService

@RestController
@PreAuthorize("hasAnyRole('ROLE_IPB_FRONTEND_RW')")
class CommunityServiceProviderController(
  private val communityServiceProviderService: CommunityServiceProviderService,
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  @Operation(summary = "Get community support services for referral select-a-service")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Paginated list of community support services",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = PageResponse::class))],
      ),
      ApiResponse(
        responseCode = "401",
        description = "The request was unauthorised",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Forbidden. The client is not authorised to access to select a referral service.",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  @GetMapping("/bff/referral-select-a-service")
  fun getServices(
    @PageableDefault(page = 0, size = 10) pageable: Pageable,
  ): ResponseEntity<PageResponse<CommunitySupportServiceDto>> {
    log.info("Fetching community support services")
    return ResponseEntity.ok(communityServiceProviderService.communityServiceProviders(pageable))
  }
}
