package uk.gov.justice.digital.hmpps.communitysupportapi.authorization

import com.microsoft.applicationinsights.TelemetryClient
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.communitysupportapi.client.ManageUsersClient
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralUser
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ServiceProvider
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ServiceProviderRepository

data class ServiceProviderAccessScope(val serviceProviders: Set<ServiceProvider>)

private data class WorkingScope(
  val authGroups: List<String>,
  val providers: MutableSet<ServiceProvider> = mutableSetOf(),
  val errors: MutableList<String> = mutableListOf(),
  val warnings: MutableList<String> = mutableListOf(),
)

data class AccessError(val user: ReferralUser, override val message: String, val errors: List<String>) : RuntimeException(message)

@Component
@Transactional
class ServiceProviderAccessScopeMapper(
  private val manageUsersClient: ManageUsersClient,
  private val serviceProviderRepository: ServiceProviderRepository,
  private val telemetryClient: TelemetryClient,
) {
  private val serviceProviderGroupPrefix = "INT_SP_"
  private val errorMessage = "could not map service provider user to access scope"

  fun fromUser(user: ReferralUser): ServiceProviderAccessScope {
    if (!isServiceProviderUser(user)) {
      throw AccessError(user, errorMessage, listOf("user is not a service provider"))
    }

    val groups = manageUsersClient.getUserGroups(user.hmppsAuthId)
      ?: throw AccessError(user, errorMessage, listOf("cannot find user in hmpps auth"))

    val workingScope = WorkingScope(authGroups = groups)

    resolveProviders(workingScope)

    blockUsersWithoutProviders(workingScope)

    if (workingScope.warnings.isNotEmpty()) {
      trackWarnings(user, workingScope.warnings)
    }
    if (workingScope.errors.isNotEmpty()) {
      throw AccessError(user, errorMessage, workingScope.errors)
    }

    return ServiceProviderAccessScope(serviceProviders = workingScope.providers).also {
      trackAuthorization(user, it)
    }
  }

  private fun trackAuthorization(user: ReferralUser, scope: ServiceProviderAccessScope) {
    telemetryClient.trackEvent(
      "InterventionsAuthorizedProvider",
      mapOf(
        "userId" to user.hmppsAuthId,
        "userName" to user.hmppsAuthUsername,
        "userAuthSource" to user.authSource,
        "providers" to scope.serviceProviders.joinToString(",") { p -> p.id.toString() },
      ),
      null,
    )
  }

  private fun trackWarnings(user: ReferralUser, scope: MutableList<String>) {
    telemetryClient.trackEvent(
      "InterventionsAuthorizationWarning",
      mapOf(
        "userId" to user.hmppsAuthId,
        "userName" to user.hmppsAuthUsername,
        "userAuthSource" to user.authSource,
        "issues" to scope.toString(),
      ),
      null,
    )
  }

  private fun resolveProviders(scope: WorkingScope) {
    val serviceProviderGroups = scope.authGroups
      .filter { it.startsWith(serviceProviderGroupPrefix) }
      .map { it.removePrefix(serviceProviderGroupPrefix) }

    val providers = getProviders(serviceProviderGroups, scope.warnings)
    scope.providers.addAll(providers)
  }

  private fun blockUsersWithoutProviders(scope: WorkingScope) {
    if (scope.providers.isEmpty()) {
      scope.errors.add("no valid service provider groups associated with user")
    }
  }

  private fun getProviders(providerGroups: List<String>, warnings: MutableList<String>): List<ServiceProvider> {
    val providers = serviceProviderRepository.findAllByAuthGroupIdIn(providerGroups)
    val unidentifiedProviders = providerGroups.subtract(providers.map { it.id }.toSet())
    unidentifiedProviders.forEach { undefinedProvider ->
      warnings.add("unidentified provider '$undefinedProvider': group does not exist in the reference data")
    }
    return providers.sortedBy { it.id }
  }

  private fun isServiceProviderUser(user: ReferralUser): Boolean = user.authSource == "auth"
}
