package uk.gov.justice.digital.hmpps.communitysupportapi.controller

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.communitysupportapi.authorization.UserMapper
import uk.gov.justice.digital.hmpps.communitysupportapi.service.LocalAppointmentFixtureService
import uk.gov.justice.hmpps.kotlin.auth.HmppsAuthenticationHolder
import java.util.*

@RestController
@Profile("local")
@Validated
@RequestMapping("/admin/local")
@PreAuthorize("hasAnyRole('ROLE_IPB_FRONTEND_RW')")
class AdminController(
  private val localAppointmentFixtureService: LocalAppointmentFixtureService,
  private val userMapper: UserMapper,
  private val authenticationHolder: HmppsAuthenticationHolder,
) {

  @PostMapping("/appointment-fixtures")
  @ResponseStatus(HttpStatus.CREATED)
  fun createAppointmentFixtures(
    @RequestParam(defaultValue = "1") @Min(1) @Max(10) count: Int,
  ): List<LocalAppointmentFixtureResponse> = localAppointmentFixtureService.createFixtures(
    count,
    userMapper.fromToken(authenticationHolder),
  )
    .map {
      LocalAppointmentFixtureResponse(
        caseReference = it.caseReference,
        referralId = it.referralId,
        crn = it.crn,
      )
    }
}

data class LocalAppointmentFixtureResponse(
  val caseReference: String,
  val referralId: UUID,
  val crn: String,
)
