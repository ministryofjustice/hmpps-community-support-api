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
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.arns.CommunitySupportRiskDto
import uk.gov.justice.digital.hmpps.communitysupportapi.service.AssessRisksAndNeedsService

@RestController
@RequestMapping("/bff/risk")
@PreAuthorize("hasAnyRole('ROLE_IPB_FRONTEND_RW')")
class RiskController(
  private val assessRisksAndNeedsService: AssessRisksAndNeedsService,
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  @Operation(summary = "Get ROSH risks for a person by CRN")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "ROSH risks found",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = CommunitySupportRiskDto::class))],
      ),
      ApiResponse(
        responseCode = "404",
        description = "ROSH risks not found for the given CRN",
        content = [Content(mediaType = "application/json")],
      ),
    ],
  )
  @GetMapping("/rosh/{crn}")
  fun getRoshRisksByCrn(
    @PathVariable crn: String,
  ): ResponseEntity<CommunitySupportRiskDto> {
    log.info("Attempt to get ROSH risks for CRN: {}", crn)

    val roshRisks = assessRisksAndNeedsService.getRoshRisksByCrn(crn)
    return ResponseEntity.ok(roshRisks)
  }
}
