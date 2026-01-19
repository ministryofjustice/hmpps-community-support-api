package uk.gov.justice.digital.hmpps.communitysupportapi.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.communitysupportapi.authorization.UserMapper
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.ReferralDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.ReferralInformationDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.toDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.toReferralInformationDto
import uk.gov.justice.digital.hmpps.communitysupportapi.exception.NotFoundException
import uk.gov.justice.digital.hmpps.communitysupportapi.model.CreateReferralRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.service.ReferralService
import java.util.UUID

@RestController
@PreAuthorize("hasAnyRole('ROLE_IPB_FRONTEND_RW')")
class ReferralController(
  private val referralService: ReferralService,
  private val userMapper: UserMapper,
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
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ReferralDto::class))],
      ),
      ApiResponse(
        responseCode = "404",
        description = "Referral not found",
        content = [Content(mediaType = "application/json")],
      ),
    ],
  )
  @GetMapping("/bff/referrals/{referralId}")
  fun getReferral(@PathVariable referralId: UUID): ResponseEntity<ReferralDto> = referralService.getReferral(referralId)
    .map { referral -> ResponseEntity.ok(referral.toDto()) }
    .orElseThrow { NotFoundException("Referral not found for id $referralId") }

  @Operation(summary = "Create a referral")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Referral created",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ReferralDto::class))],
      ),
    ],
  )
  @PostMapping("/bff/referrals")
  fun createReferral(@RequestBody createReferralRequest: CreateReferralRequest, authentication: JwtAuthenticationToken): ResponseEntity<ReferralInformationDto> {
    val user = userMapper.fromToken(authentication)
    val result = referralService.createReferral(user, createReferralRequest)
    return ResponseEntity.ok(result.toReferralInformationDto())
  }
}
