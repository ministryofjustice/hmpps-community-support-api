package uk.gov.justice.digital.hmpps.communitysupportapi.service

import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.communitysupportapi.authorization.ServiceProviderAccessScopeMapper
import uk.gov.justice.digital.hmpps.communitysupportapi.authorization.UserMapper
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.ReferralCaseListDto
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.CaseListView
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.CaseListViewRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.specification.CaseListViewSpecifications
import uk.gov.justice.hmpps.kotlin.auth.HmppsAuthenticationHolder

@Service
class CaseListService(
  private val caseListViewRepository: CaseListViewRepository,
  private val authenticationHolder: HmppsAuthenticationHolder,
  private val userMapper: UserMapper,
  private val serviceProviderAccessScopeMapper: ServiceProviderAccessScopeMapper,
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  private fun getCasesByState(
    pageable: Pageable,
    stateSpecification: Specification<CaseListView>,
    logMessage: String,
  ): Page<CaseListView> {
    if (authenticationHolder.username.isNullOrBlank()) {
      throw AuthenticationCredentialsNotFoundException("No authenticated user found")
    }

    val referralUser = userMapper.fromToken(authenticationHolder)
    if (referralUser.authSource.equals("delius", ignoreCase = true)) {
      log.info("Delius user detected: ${authenticationHolder.username} - returning empty case list")
      return Page.empty()
    }

    log.info("$logMessage: ${referralUser.hmppsAuthUsername}")

    val serviceProviders =
      serviceProviderAccessScopeMapper.fromUser(referralUser).serviceProviders

    val specification = CaseListViewSpecifications
      .hasServiceProviderIn(serviceProviders)
      .and(stateSpecification)

    return caseListViewRepository.findAll(specification, pageable)
  }

  fun getUnassignedCases(pageable: Pageable): Page<ReferralCaseListDto> = getCasesByState(
    pageable = pageable,
    stateSpecification = CaseListViewSpecifications.isUnassigned(),
    logMessage = "Fetching unassigned cases for user",
  ).map { ReferralCaseListDto.from(it) }

  fun getInProgressCases(pageable: Pageable): Page<ReferralCaseListDto> = getCasesByState(
    pageable = pageable,
    stateSpecification = CaseListViewSpecifications.isAssigned(),
    logMessage = "Fetching in-progress cases for user",
  ).map { ReferralCaseListDto.from(it) }
}
