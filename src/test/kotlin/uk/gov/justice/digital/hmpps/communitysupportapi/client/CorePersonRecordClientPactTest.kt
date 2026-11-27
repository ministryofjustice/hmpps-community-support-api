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
          identifiers.array("crns") { crns ->
            crns.stringType(crn)
            crns.stringType("X999999")
          }
          identifiers.array("prisonNumbers") { prisonNumbers -> prisonNumbers.stringType("B9876ZY") }
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
        body.array("nationalities") { nationalities ->
          nationalities.`object` { nationality ->
            nationality.stringType("description", "GB")
          }
        }
        body.array("addresses") { addresses ->
          addresses.`object` { address ->
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
            address.array("usages") { usages ->
              usages.`object` { usage ->
                usage.stringType("description", "Curfew Order")
                usage.booleanType("isActive", true)
              }
            }
            address.array("contacts") { contacts ->
              contacts.`object` { contact ->
                contact.`object`("type") { type ->
                  type.stringType("code", "HOME")
                }
                contact.stringType("value", "+44 20 7946 0000")
              }
              contacts.`object` { contact ->
                contact.`object`("type") { type ->
                  type.stringType("code", "TELEPHONE")
                }
                contact.stringType("value", "+44 20 7946 0001")
              }
              contacts.`object` { contact ->
                contact.`object`("type") { type ->
                  type.stringType("code", "MOBILE")
                }
                contact.stringType("value", "+44 7700 900002")
              }
              contacts.`object` { contact ->
                contact.`object`("type") { type ->
                  type.stringType("code", "EMAIL")
                }
                contact.stringType("value", "john.doe@example.com")
              }
            }
          }
        }
        body.`object`("identifiers") { identifiers ->
          identifiers.array("prisonNumbers") { prisonNumbers ->
            prisonNumbers.stringType(prisonNumber)
            prisonNumbers.stringType("B5678DE")
          }
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
    assertEquals(listOf(prisonNumber, "B5678DE"), person.prisonNumbers)
    assertEquals("Male", person.sex)
    assertEquals(listOf("GB"), additionalDetails.nationalities)
    assertEquals("Black/Black British : Any other backgr'nd", additionalDetails.ethnicity)
    assertEquals("Christianity", additionalDetails.religionOrBelief)
    assertEquals("+44 20 7946 0001", additionalDetails.phoneNumber)
    assertEquals("+44 7700 900002", additionalDetails.mobileNumber)
    assertEquals("john.doe@example.com", additionalDetails.emailAddress)
    assertEquals(
      "102, Main Building, Petty France, Westminster, London, Greater London, SW1H 9AJ",
      additionalDetails.address,
    )
    assertEquals("Curfew Order", additionalDetails.addressType)
    assertEquals(LocalDate.of(2020, 2, 26), additionalDetails.addressStartDate)
    assertEquals("Some comment", additionalDetails.addressNotes)
  }
}
