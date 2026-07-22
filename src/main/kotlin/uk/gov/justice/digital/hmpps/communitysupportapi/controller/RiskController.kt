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
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.communitysupportapi.authorization.UserMapper
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.CommunitySupportRiskInformationDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.arns.CommunitySupportRiskDto
import uk.gov.justice.digital.hmpps.communitysupportapi.service.RiskInformationService
import uk.gov.justice.hmpps.kotlin.auth.HmppsAuthenticationHolder
import java.util.UUID

@RestController
@PreAuthorize("hasAnyRole('ROLE_IPB_FRONTEND_RW')")
class RiskController(
  private val riskInformationService: RiskInformationService,
  private val userMapper: UserMapper,
  private val authenticationHolder: HmppsAuthenticationHolder,
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  @Operation(summary = "Get ROSH risks for a referral")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "ROSH risks found",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = CommunitySupportRiskDto::class))],
      ),
      ApiResponse(
        responseCode = "404",
        description = "Referral not found, or ROSH risks not found for the referral's CRN",
        content = [Content(mediaType = "application/json")],
      ),
    ],
  )
  @GetMapping("/bff/risk/rosh/{referralId}")
  fun getRoshRisksByReferralId(
    @PathVariable referralId: UUID,
  ): ResponseEntity<CommunitySupportRiskDto> {
    log.info("Attempt to get ROSH risks for referral: {}", referralId)

    val roshRisks = riskInformationService.getRoshRisksByReferralId(referralId)
    return ResponseEntity.ok(roshRisks)
  }

  @Operation(summary = "Save draft OASys risk information for a referral")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Save Risk information",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = CommunitySupportRiskInformationDto::class))],
      ),
      ApiResponse(
        responseCode = "404",
        description = "Referral not found",
        content = [Content(mediaType = "application/json")],
      ),
    ],
  )
  @PutMapping("/risk-information/{referralId}")
  fun saveRiskInformation(
    @PathVariable referralId: UUID,
    @RequestBody request: CommunitySupportRiskInformationDto,
  ): ResponseEntity<CommunitySupportRiskInformationDto> {
    log.info("Attempt to save risk information for referral: {}", referralId)

    val user = userMapper.fromToken(authenticationHolder)
    val result = riskInformationService.saveDraftRiskInformation(referralId, user.id, request)
    return ResponseEntity.ok(result)
  }
}
