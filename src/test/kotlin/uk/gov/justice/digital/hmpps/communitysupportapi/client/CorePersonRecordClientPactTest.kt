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
        body.stringType("cprUUID", "f91ef118-a51f-4874-9409-c0538b4ca6fd")
        body.stringType("firstName", "John")
        body.stringType("middleNames", "Morgan")
        body.stringType("lastName", "Doe")
        body.date("dateOfBirth", "yyyy-MM-dd", LocalDate.of(1990, 8, 21))
        body.booleanType("disability", true)
        body.booleanType("interestToImmigration", true)
        body.`object`("title") { title ->
          title.stringType("code", "Mr")
          title.stringType("description", "Mr")
        }
        body.`object`("sex") { sex ->
          sex.stringType("code", "M")
          sex.stringType("description", "Male")
        }
        body.`object`("sexualOrientation") { orientation ->
          orientation.stringType("code", "HET")
          orientation.stringType("description", "Heterosexual")
        }
        body.`object`("religion") { religion ->
          religion.stringType("code", "Christianity")
          religion.stringType("description", "Christianity")
        }
        body.`object`("ethnicity") { ethnicity ->
          ethnicity.stringType("code", "B9")
          ethnicity.stringType("description", "Black/Black British : Any other backgr'nd")
        }
        body.array("aliases") { aliases ->
          aliases.`object` { alias ->
            alias.stringType("firstName", "Jon")
            alias.stringType("lastName", "do")
            alias.stringType("middleNames", "Morgain")
            alias.`object`("title") { title ->
              title.stringType("code", "Mr")
              title.stringType("description", "Mr")
            }
            alias.`object`("sex") { sex ->
              sex.stringType("code", "M")
              sex.stringType("description", "Male")
            }
          }
        }
        body.array("nationalities") { nationalities ->
          nationalities.`object` { nationality ->
            nationality.stringType("code", "GB")
            nationality.stringType("description", "GB")
          }
        }
        body.array("addresses") { addresses ->
          addresses.`object` { address ->
            address.stringType("cprAddressId", "ec4c7479-218c-4f11-a02d-edd749820679")
            address.booleanType("noFixedAbode", false)
            address.date("startDate", "yyyy-MM-dd", LocalDate.of(2020, 2, 26))
            address.stringType("startDateTime", "2026-02-26T11:08:46.347Z")
            address.date("endDate", "yyyy-MM-dd", LocalDate.of(2023, 7, 15))
            address.stringType("endDateTime", "2026-07-15T11:08:46.347Z")
            address.stringType("postcode", "SW1H 9AJ")
            address.stringType("subBuildingName", "Sub building 2")
            address.stringType("buildingName", "Main Building")
            address.stringType("buildingNumber", "102")
            address.stringType("thoroughfareName", "Petty France")
            address.stringType("dependentLocality", "Westminster")
            address.stringType("postTown", "London")
            address.stringType("county", "Greater London")
            address.stringType("country", "United Kingdom of Great Britain and Northern Ireland (the)")
            address.stringType("countryCode", "GBR")
            address.stringType("uprn", "100120991537")
            address.`object`("status") { status ->
              status.stringType("code", "M")
              status.stringType("description", "Main")
            }
            address.stringType("comment", "Some comment")
            address.booleanType("typeVerified", false)
            address.array("usages") { usages ->
              usages.`object` { usage ->
                usage.stringType("code", "CURFEW")
                usage.stringType("description", "Curfew Order")
                usage.booleanType("isActive", true)
                usage.stringType("createDateTime", "2023-01-01T12:00:00")
                usage.stringType("createUserId", "string")
                usage.stringType("modifyDateTime", "2023-01-01T12:00:00")
                usage.stringType("modifyUserId", "string")
              }
            }
            address.array("contacts") { contacts ->
              contacts.`object` { contact ->
                contact.stringType("cprContactId", "aa7299c5-0201-4cab-be25-e4eb9d1ac655")
                contact.`object`("type") { type ->
                  type.stringType("code", "HOME")
                  type.stringType("description", "Home")
                }
                contact.stringType("value", "+44 20 7946 0000")
                contact.stringType("extension", "1234")
                contact.stringType("createDateTime", "2023-01-01T12:00:00")
                contact.stringType("createUserId", "string")
                contact.stringType("modifyDateTime", "2023-01-01T12:00:00")
                contact.stringType("modifyUserId", "string")
              }
            }
            address.stringType("createDateTime", "2023-01-01T12:00:00")
            address.stringType("createUserId", "string")
            address.stringType("modifyDateTime", "2023-01-01T12:00:00")
            address.stringType("modifyUserId", "string")
          }
        }
        body.`object`("identifiers") { identifiers ->
          identifiers.array("crns") { crns -> crns.stringType("B123435") }
          identifiers.array("prisonNumbers") { prisonNumbers -> prisonNumbers.stringType(prisonNumber) }
          identifiers.array("cids") { cids -> cids.stringType("\"1234567\"") }
          identifiers.array("pncs") { pncs -> pncs.stringType("2000/1234567A") }
          identifiers.array("cros") { cros -> cros.stringType("123456/00A") }
          identifiers.array("nationalInsuranceNumbers") { numbers -> numbers.stringType("QQ123456B") }
          identifiers.array("driverLicenseNumbers") { numbers -> numbers.stringType("SMITH840325J912") }
          identifiers.array("arrestSummonsNumbers") { numbers -> numbers.stringType("0700000000000002536Y") }
          identifiers.array("otherIdentifiers") { ids -> ids.stringType("string") }
        }
        body.array("contacts") { contacts ->
          contacts.`object` { contact ->
            contact.stringType("cprContactId", "aa7299c5-0201-4cab-be25-e4eb9d1ac655")
            contact.`object`("type") { type ->
              type.stringType("code", "HOME")
              type.stringType("description", "Home")
            }
            contact.stringType("value", "+44 20 7946 0000")
            contact.stringType("extension", "1234")
            contact.stringType("createDateTime", "2023-01-01T12:00:00")
            contact.stringType("createUserId", "string")
            contact.stringType("modifyDateTime", "2023-01-01T12:00:00")
            contact.stringType("modifyUserId", "string")
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
    assertEquals(true, person.disability)
    assertEquals("Mr", person.title?.code)
    assertEquals(listOf(prisonNumber), person.identifiers.prisonNumbers)
    assertEquals(listOf("GB"), person.nationalities.map { it.code })
    assertEquals(1, person.aliases.size)
    assertEquals("Jon", person.aliases.first().firstName)
    assertEquals(1, person.addresses.size)
    assertEquals("SW1H 9AJ", person.addresses.first().postcode)
    assertEquals("CURFEW", person.addresses.first().usages.first().code)
  }
}
