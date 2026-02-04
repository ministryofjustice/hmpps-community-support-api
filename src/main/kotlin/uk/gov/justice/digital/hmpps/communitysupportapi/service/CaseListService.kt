package uk.gov.justice.digital.hmpps.communitysupportapi.service

import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.communitysupportapi.authorization.ServiceProviderAccessScopeMapper
import uk.gov.justice.digital.hmpps.communitysupportapi.authorization.UserMapper
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

  fun getUnassignedCases(pageable: Pageable): Page<CaseListView> {
    if (authenticationHolder.username.isNullOrBlank()) {
      throw AuthenticationCredentialsNotFoundException("No authenticated user found")
    }
    if (authenticationHolder.authSource.name.equals("delius", ignoreCase = true)) {
      log.info("Delius user detected: ${authenticationHolder.username} - returning empty case list")
      return Page.empty()
    }
    val referralUser = userMapper.fromToken(authenticationHolder)
    log.info("Fetching unassigned cases for user: ${referralUser.hmppsAuthUsername}")

    val accessScope = serviceProviderAccessScopeMapper.fromUser(referralUser)
    val serviceProviders = accessScope.serviceProviders

    val specification = CaseListViewSpecifications.hasServiceProviderIn(serviceProviders)
      .and(CaseListViewSpecifications.isUnassigned())

    return caseListViewRepository.findAll(specification, pageable)
  }
}
