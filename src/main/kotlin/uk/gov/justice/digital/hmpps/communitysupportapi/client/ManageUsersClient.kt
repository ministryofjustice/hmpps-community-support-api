package uk.gov.justice.digital.hmpps.communitysupportapi.client

import io.netty.channel.ConnectTimeoutException
import io.netty.handler.timeout.ReadTimeoutException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatusCode
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToFlux
import org.springframework.web.reactive.function.client.bodyToMono
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.util.retry.Retry
import reactor.util.retry.Retry.RetrySignal
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AuthGroupID
import java.util.UUID

private data class AuthGroupResponse(
  val groupCode: String,
  val groupName: String,
)

data class UserDetail(
  val username: String,
  val active: Boolean,
  val authSource: String,
  val name: String,
  val userId: String,
  val uuid: UUID? = null,
)

@Component
class ManageUsersClient(
  @Value($$"${services.manage-users-api.locations.auth-user-groups}") private val authUserGroupsLocation: String,
  @Value($$"${services.manage-users-api.locations.auth-user-details}") private val authUserDetailLocation: String,
  @Value($$"${webclient.manage-users-api.max-retry-attempts}") private val maxRetryAttempts: Long,
  @Qualifier("manageUsersWebClient") private val manageUsersApiClient: WebClient,
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  fun getUserGroups(userId: String): List<AuthGroupID>? {
    val url = UriComponentsBuilder.fromPath(authUserGroupsLocation)
      .buildAndExpand(userId)
      .toString()

    return manageUsersApiClient.get()
      .uri(url)
      .retrieve()
      .onStatus(HttpStatusCode::is4xxClientError) { response ->
        if (response.statusCode().value() == 404) {
          Mono.empty()
        } else {
          response.createException()
        }
      }
      .bodyToFlux<AuthGroupResponse>()
      .withRetryPolicy()
      .map { it.groupCode }
      .collectList()
      .block()
  }

  fun getUserDetails(userName: String): UserDetail? {
    val url = UriComponentsBuilder.fromPath(authUserDetailLocation)
      .buildAndExpand(userName)
      .toString()

    return manageUsersApiClient.get()
      .uri(url)
      .retrieve()
      .onStatus(HttpStatusCode::is4xxClientError) { response ->
        if (response.statusCode().value() == 404) {
          Mono.empty()
        } else {
          response.createException()
        }
      }
      .bodyToMono<UserDetail>()
      .withRetryPolicy()
      .block()
  }

  private fun retryPolicy(): Retry = Retry.max(maxRetryAttempts)
    .filter { isTimeoutException(it) }
    .doBeforeRetry { logRetrySignal(it) }

  private fun <T : Any> Flux<T>.withRetryPolicy(): Flux<T> = this.retryWhen(retryPolicy())

  private fun <T : Any> Mono<T>.withRetryPolicy(): Mono<T> = this.retryWhen(retryPolicy())

  private fun isTimeoutException(it: Throwable): Boolean = it is ReadTimeoutException ||
    it is ConnectTimeoutException ||
    it.cause is ReadTimeoutException ||
    it.cause is ConnectTimeoutException

  private fun logRetrySignal(retrySignal: RetrySignal) {
    val exception = retrySignal.failure()?.cause ?: retrySignal.failure()
    val message = exception.message ?: exception.javaClass.canonicalName
    log.debug(
      "Retrying due to [$message]",
      exception,
    )
  }
}
