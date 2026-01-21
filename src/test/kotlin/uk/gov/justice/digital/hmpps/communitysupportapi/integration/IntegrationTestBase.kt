package uk.gov.justice.digital.hmpps.communitysupportapi.integration

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.reactive.server.WebTestClient
import org.testcontainers.containers.PostgreSQLContainer
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.wiremock.HmppsAuthApiExtension
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.wiremock.HmppsAuthApiExtension.Companion.hmppsAuth
import uk.gov.justice.hmpps.test.kotlin.auth.JwtAuthorisationHelper

@ExtendWith(HmppsAuthApiExtension::class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureWebTestClient
@Import(TestWebClientConfiguration::class)
@ActiveProfiles("test")
abstract class IntegrationTestBase {

  @Autowired
  protected lateinit var webTestClient: WebTestClient

  @Autowired
  protected lateinit var jwtAuthHelper: JwtAuthorisationHelper

  @BeforeEach
  fun setup() {
    wireMockServer.resetRequests()
    stubAuthTokenEndpoint()
    stubPingWithResponse(200)
  }

  companion object {

    @JvmStatic
    private val postgresContainer = PostgreSQLContainer<Nothing>("postgres:17")
      .apply {
        withUsername("admin")
        withPassword("admin_password")
        withReuse(true)
      }

    @JvmStatic
    val wireMockServer = WireMockServer(WireMockConfiguration.options().dynamicPort())

    @BeforeAll
    @JvmStatic
    fun startContainers() {
      postgresContainer.start()
      wireMockServer.start()
      WireMock.configureFor("localhost", wireMockServer.port())
    }

    @DynamicPropertySource
    @JvmStatic
    fun setUpProperties(registry: DynamicPropertyRegistry) {
      registry.add("spring.datasource.url") { postgresContainer.jdbcUrl }
      registry.add("spring.datasource.username") { postgresContainer.username }
      registry.add("spring.datasource.password") { postgresContainer.password }
      registry.add("services.ndelius-integration-api.base-url") { "http://localhost:${wireMockServer.port()}" }
      registry.add("services.oasys-api.base-url") { "http://localhost:${wireMockServer.port()}" }
      registry.add("hmpps-auth.url") { "http://localhost:8090/auth" }
    }
  }

  internal fun setAuthorisation(
    username: String? = "AUTH_ADM",
    roles: List<String> = listOf("ROLE_IPB_FRONTEND_RW"),
    scopes: List<String> = listOf("read"),
  ): (HttpHeaders) -> Unit = jwtAuthHelper.setAuthorisationHeader(username = username, scope = scopes, roles = roles)

  protected fun stubPingWithResponse(status: Int) {
    hmppsAuth.stubHealthPing(status)
  }

  fun stubAuthTokenEndpoint() {
    hmppsAuth.stubGrantToken()
  }
}
