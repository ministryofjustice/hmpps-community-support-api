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
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.communitysupportapi.authorization.UserMapper
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.CaseWorkerDto
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.UserType
import uk.gov.justice.digital.hmpps.communitysupportapi.model.AssignCaseWorkersRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.model.AssignCaseWorkersResult
import uk.gov.justice.digital.hmpps.communitysupportapi.service.ReferralAssignmentService
import uk.gov.justice.digital.hmpps.communitysupportapi.service.ReferralService
import uk.gov.justice.hmpps.kotlin.auth.HmppsAuthenticationHolder
import java.util.UUID

@RestController
@PreAuthorize("hasAnyRole('ROLE_IPB_FRONTEND_RW')")
class ReferralUserAssignmentController(
  private val referralService: ReferralService,
  private val referralAssignmentService: ReferralAssignmentService,
  private val authenticationHolder: HmppsAuthenticationHolder,
  private val userMapper: UserMapper,
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  @Operation(summary = "Assign case workers to a referral")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Assign case workers to a referral",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = AssignCaseWorkersResult::class))],
      ),
      ApiResponse(
        responseCode = "400",
        description = "Failed to assign case worker(s)",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = AssignCaseWorkersResult::class))],
      ),
      ApiResponse(
        responseCode = "404",
        description = "Referral not found",
        content = [Content(mediaType = "application/json")],
      ),
    ],
  )
  @PostMapping("/bff/referral/{referralId}/assign")
  fun assignCaseWorkers(@PathVariable referralId: String, @RequestBody assignCaseWorkersRequest: AssignCaseWorkersRequest, authentication: JwtAuthenticationToken): ResponseEntity<AssignCaseWorkersResult> {
    val user = userMapper.fromToken(authenticationHolder)

    val emailList = assignCaseWorkersRequest.emails
      .map { email ->
        CaseWorkerDto(userType = UserType.EXTERNAL, fullName = "", emailAddress = email.trim().lowercase())
      }

    val result = referralAssignmentService.assignCaseWorkers(user, UUID.fromString(referralId), emailList)

    return when {
      (result?.success == true) -> ResponseEntity.ok(result)
      else -> ResponseEntity.badRequest().body(result)
    }
  }
}
