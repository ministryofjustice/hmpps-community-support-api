package uk.gov.justice.digital.hmpps.communitysupportapi.client

import au.com.dius.pact.consumer.MockServer
import au.com.dius.pact.consumer.dsl.LambdaDsl.newJsonBody
import au.com.dius.pact.consumer.dsl.PactDslJsonRootValue
import au.com.dius.pact.consumer.dsl.PactDslWithProvider
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt
import au.com.dius.pact.consumer.junit5.PactTestFor
import au.com.dius.pact.core.model.PactSpecVersion
import au.com.dius.pact.core.model.RequestResponsePact
import au.com.dius.pact.core.model.annotations.Pact
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.communitysupportapi.mapper.toAdditionalDetails
import uk.gov.justice.digital.hmpps.communitysupportapi.mapper.toPrisonPerson
import uk.gov.justice.digital.hmpps.communitysupportapi.mapper.toProbationPerson
import uk.gov.justice.digital.hmpps.communitysupportapi.model.PersonIdentifier
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
          sex.stringType("description", "Male")
        }
        body.`object`("identifiers") { identifiers ->
          identifiers.minArrayLike("crns", 1, PactDslJsonRootValue.stringType(crn), 1)
          identifiers.minArrayLike("prisonNumbers", 0, PactDslJsonRootValue.stringType("B9876ZY"), 1)
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
        body.stringType("middleNames", "Morgan")
        body.stringType("lastName", "Doe")
        body.date("dateOfBirth", "yyyy-MM-dd", LocalDate.of(1990, 8, 21))
        body.`object`("sex") { sex ->
          sex.stringType("description", "Male")
        }
        body.`object`("religion") { religion ->
          religion.stringType("description", "Christianity")
        }
        body.`object`("ethnicity") { ethnicity ->
          ethnicity.stringType("description", "Black/Black British : Any other backgr'nd")
        }
        body.minArrayLike("nationalities", 0, 1) { nationality ->
          nationality.stringType("description", "GB")
        }
        body.minArrayLike("addresses", 0, 1) { address ->
          address.stringType("cprAddressId", "ec4c7479-218c-4f11-a02d-edd749820679")
          address.date("startDate", "yyyy-MM-dd", LocalDate.of(2020, 2, 26))
          address.stringType("buildingName", "Main Building")
          address.stringType("buildingNumber", "102")
          address.stringType("thoroughfareName", "Petty France")
          address.stringType("dependentLocality", "Westminster")
          address.stringType("postTown", "London")
          address.stringType("county", "Greater London")
          address.stringType("postcode", "SW1H 9AJ")
          address.`object`("status") { status ->
            status.stringType("code", "M")
          }
          address.stringType("comment", "Some comment")
          address.minArrayLike("usages", 0, 1) { usage ->
            usage.stringType("description", "Curfew Order")
            usage.booleanType("isActive", true)
          }
          address.minArrayLike("contacts", 0, 1) { contact ->
            contact.`object`("type") { type ->
              type.stringType("code", "TELEPHONE")
            }
            contact.stringType("value", "+44 20 7946 0001")
          }
        }
        body.`object`("identifiers") { identifiers ->
          identifiers.minArrayLike(
            "prisonNumbers",
            1,
            PactDslJsonRootValue.stringType(prisonNumber),
            1,
          )
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

    val person = client.getPersonByCrn(crn).toProbationPerson()

    assertEquals(PersonIdentifier.Crn(crn), person.identifier)
    assertEquals("John", person.firstName)
    assertEquals("James", person.middleNames)
    assertEquals("Smith", person.lastName)
    assertEquals(LocalDate.of(1980, 1, 1), person.dateOfBirth)
    assertEquals("Male", person.sex)
    assertEquals(listOf("B9876ZY"), person.prisonNumbers)
  }

  @Test
  @PactTestFor(pactMethod = "getPrisonPerson")
  fun `get person by prison number`(mockServer: MockServer) {
    val client = createCorePersonRecordClient(mockServer.getUrl())

    val response = client.getPersonByPrisonNumber(prisonNumber)
    val person = response.toPrisonPerson()
    val additionalDetails = response.toAdditionalDetails()

    assertEquals(PersonIdentifier.PrisonerNumber(prisonNumber), person.identifier)
    assertEquals("John", person.firstName)
    assertEquals("Morgan", person.middleNames)
    assertEquals("Doe", person.lastName)
    assertEquals(LocalDate.of(1990, 8, 21), person.dateOfBirth)
    assertEquals(listOf(prisonNumber), person.prisonNumbers)
    assertEquals("Male", person.sex)
    assertEquals(listOf("GB"), additionalDetails.nationalities)
    assertEquals("Black/Black British : Any other backgr'nd", additionalDetails.ethnicity)
    assertEquals("Christianity", additionalDetails.religionOrBelief)
    assertEquals("+44 20 7946 0001", additionalDetails.phoneNumber)
    assertNull(additionalDetails.mobileNumber)
    assertNull(additionalDetails.emailAddress)
    assertEquals(
      "102, Main Building, Petty France, Westminster, London, Greater London, SW1H 9AJ",
      additionalDetails.address,
    )
    assertEquals("Curfew Order", additionalDetails.addressType)
    assertEquals(LocalDate.of(2020, 2, 26), additionalDetails.addressStartDate)
    assertEquals("Some comment", additionalDetails.addressNotes)
  }
}
