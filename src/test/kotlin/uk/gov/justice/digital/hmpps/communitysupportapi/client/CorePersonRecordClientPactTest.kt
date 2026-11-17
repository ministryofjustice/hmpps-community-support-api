package uk.gov.justice.digital.hmpps.communitysupportapi.client

import au.com.dius.pact.consumer.MockServer
import au.com.dius.pact.consumer.dsl.LambdaDsl.newJsonBody
import au.com.dius.pact.consumer.dsl.PactDslWithProvider
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt
import au.com.dius.pact.consumer.junit5.PactTestFor
import au.com.dius.pact.core.model.PactSpecVersion
import au.com.dius.pact.core.model.RequestResponsePact
import au.com.dius.pact.core.model.annotations.Pact
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.web.reactive.function.client.WebClient
import java.time.LocalDate

private const val CONSUMER = "hmpps-community-support-api"
private const val PROVIDER = "hmpps-person-record"

@ExtendWith(PactConsumerTestExt::class)
@PactTestFor(providerName = PROVIDER, pactVersion = PactSpecVersion.V3)
class CorePersonRecordClientPactTest {
  private val crn = "X123456"
  private val prisonNumber = "A1234AA"
  private val jsonHeaders = mapOf("Content-Type" to "application/json")

  @Pact(consumer = CONSUMER, provider = PROVIDER)
  fun getProbationPerson(builder: PactDslWithProvider): RequestResponsePact = builder
    .given("A probation person exists for the requested CRN")
    .uponReceiving("a request for a probation person by CRN")
    .pathFromProviderState($$"/person/probation/${crn}", "/person/probation/$crn")
    .method("GET")
    .willRespondWith()
    .status(200)
    .headers(jsonHeaders)
    .body(
      newJsonBody { body ->
        body.stringType("firstName", "John")
        body.stringType("middleNames", "James")
        body.stringType("lastName", "Smith")
        body.date("dateOfBirth", "yyyy-MM-dd", LocalDate.of(1980, 1, 1))
        body.`object`("sex") { sex ->
          sex.stringType("code", "M")
          sex.stringType("description", "Male")
        }
        body.`object`("identifiers") { identifiers ->
          identifiers.array("crns") { crns -> crns.stringType(crn) }
          identifiers.array("pncs") { pncs -> pncs.stringType("PNC123") }
        }
        body.array("addresses") { addresses ->
          addresses.`object` { address ->
            address.stringType("cprAddressId", "123e4567-e89b-12d3-a456-426614174001")
            address.booleanType("noFixedAbode", false)
            address.date("startDate", "yyyy-MM-dd", LocalDate.of(2025, 1, 1))
            address.stringType("postcode", "SW1A 1AA")
            address.stringType("buildingNumber", "1")
            address.stringType("thoroughfareName", "Some Street")
            address.stringType("postTown", "London")
            address.`object`("status") { status ->
              status.stringType("code", "M")
              status.stringType("description", "Main")
            }
          }
        }
      }.build(),
    )
    .toPact()

  @Pact(consumer = CONSUMER, provider = PROVIDER)
  fun getPrisonPerson(builder: PactDslWithProvider): RequestResponsePact = builder
    .given("A person exists for the requested prisoner number")
    .uponReceiving("a request for a person by prison number")
    .pathFromProviderState($$"/person/prison/${prisonerNumber}", "/person/prison/$prisonNumber")
    .method("GET")
    .willRespondWith()
    .status(200)
    .headers(jsonHeaders)
    .body(
      newJsonBody { body ->
        body.stringType("firstName", "John")
        body.stringType("middleNames", "James")
        body.stringType("lastName", "Smith")
        body.date("dateOfBirth", "yyyy-MM-dd", LocalDate.of(1980, 1, 1))
        body.`object`("sex") { sex ->
          sex.stringType("code", "M")
          sex.stringType("description", "Male")
        }
        body.`object`("identifiers") { identifiers ->
          identifiers.array("prisonNumbers") { prisonNumbers -> prisonNumbers.stringType(prisonNumber) }
        }
      }.build(),
    )
    .toPact()

  private fun createCorePersonRecordClient(baseUrl: String): CorePersonRecordClient = CorePersonRecordClient(
    WebClient.builder().baseUrl(baseUrl).build(),
  )

  @Test
  @PactTestFor(pactMethod = "getProbationPerson")
  fun `get probation person by CRN`(mockServer: MockServer) {
    val client = createCorePersonRecordClient(mockServer.getUrl())

    val person = client.getPersonByCrn(crn)

    assertEquals("John", person.firstName)
    assertEquals("James", person.middleNames)
    assertEquals("Smith", person.lastName)
    assertEquals("M", person.sex?.code)
    assertEquals("Male", person.sex?.description)
    assertEquals(listOf(crn), person.identifiers.crns)
    assertEquals(1, person.addresses.size)
  }

  @Test
  @PactTestFor(pactMethod = "getPrisonPerson")
  fun `get person by prison number`(mockServer: MockServer) {
    val client = createCorePersonRecordClient(mockServer.getUrl())

    val person = client.getPersonByPrisonNumber(prisonNumber)

    assertEquals("John", person.firstName)
    assertEquals("James", person.middleNames)
    assertEquals("Smith", person.lastName)
    assertEquals(listOf(prisonNumber), person.identifiers.prisonNumbers)
  }
}
