package uk.gov.justice.digital.hmpps.communitysupportapi.testdata

import uk.gov.justice.digital.hmpps.communitysupportapi.dto.cpr.CprAddressDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.cpr.CprAddressUsageDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.cpr.CprCodeDescriptionDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.cpr.CprContactDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.cpr.CprIdentifiersDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.cpr.CprPersonDto
import uk.gov.justice.digital.hmpps.communitysupportapi.model.PersonAdditionalDetails
import uk.gov.justice.digital.hmpps.communitysupportapi.util.toJson
import uk.gov.service.notify.SendEmailResponse
import java.time.LocalDate
import java.util.UUID
import kotlin.String

object ExternalApiResponse {

  const val CRN = "X123456"
  const val PRISONER_NUMBER = "A1234BC"

  // CPR PROBATION PERSON DATA

  fun createCprProbationPersonDto(crn: String): CprPersonDto = CprPersonDto(
    cprUUID = null,
    firstName = "John",
    middleNames = "David",
    lastName = "Smith",
    dateOfBirth = "1985-01-01",
    title = CprCodeDescriptionDto(code = "MR", description = "Mr"),
    sex = CprCodeDescriptionDto(code = "M", description = "Male"),
    ethnicity = CprCodeDescriptionDto(code = "W1", description = "White"),
    religion = CprCodeDescriptionDto(code = "CHR", description = "Christian"),
    sexualOrientation = CprCodeDescriptionDto(code = "HET", description = "Heterosexual"),
    disability = true,
    nationalities = listOf(
      CprCodeDescriptionDto(code = "ARG", description = "Argentine"),
      CprCodeDescriptionDto(code = "BRA", description = "Brazilian"),
    ),
    addresses = listOf(
      CprAddressDto(
        cprAddressId = "addr-probation-1",
        buildingNumber = "1",
        thoroughfareName = "Test Street",
        postTown = "Testville",
        postcode = "TE1 1ST",
        startDate = "2005-12-01",
        comment = "No notes",
        status = CprCodeDescriptionDto(code = "M", description = "Main"),
        usages = listOf(
          CprAddressUsageDto(
            code = "FF",
            description = "Friends/Family (settled) (verified)",
            isActive = true,
          ),
        ),
        contacts = listOf(
          CprContactDto(
            type = CprCodeDescriptionDto(code = "TELEPHONE", description = "Telephone"),
            value = "01234567890",
          ),
          CprContactDto(
            type = CprCodeDescriptionDto(code = "MOBILE", description = "Mobile"),
            value = "07700900002",
          ),
          CprContactDto(
            type = CprCodeDescriptionDto(code = "EMAIL", description = "Email"),
            value = "john.smith@example.com",
          ),
        ),
      ),
    ),
    identifiers = CprIdentifiersDto(
      crns = listOf(crn),
      prisonNumbers = emptyList(),
      pncs = listOf("2012/0052494Q"),
      cros = listOf("123456/24A"),
      nationalInsuranceNumbers = listOf("AA123456A"),
    ),
  )

  fun createCprProbationPersonAdditionalDetails(): PersonAdditionalDetails = PersonAdditionalDetails(
    ethnicity = "White",
    preferredLanguage = null,
    neurodiverseConditions = null,
    religionOrBelief = "Christian",
    transgender = null,
    sexualOrientation = "Heterosexual",
    genderIdentity = "Male",
    nationalities = listOf("Argentine", "Brazilian"),
    address = "1, Test Street, Testville, TE1 1ST",
    addressType = "Friends/Family (settled) (verified)",
    addressStartDate = LocalDate.of(2005, 12, 1),
    addressNotes = "No notes",
    phoneNumber = "01234567890",
    mobileNumber = "07700900002",
    emailAddress = "john.smith@example.com",
    disability = true,
  )

  // CPR PRISON PERSON DATA

  fun createCprPrisonPersonDto(prisonNumber: String): CprPersonDto = CprPersonDto(
    cprUUID = null,
    firstName = "John",
    middleNames = "James",
    lastName = "Smith",
    dateOfBirth = "1985-01-01",
    title = CprCodeDescriptionDto(code = "MR", description = "Mr"),
    sex = CprCodeDescriptionDto(code = "M", description = "Male"),
    ethnicity = CprCodeDescriptionDto(code = "W1", description = "White"),
    religion = CprCodeDescriptionDto(code = "CHR", description = "Christian"),
    sexualOrientation = CprCodeDescriptionDto(code = "HET", description = "Heterosexual"),
    disability = false,
    nationalities = listOf(
      CprCodeDescriptionDto(code = "GBR", description = "British"),
    ),
    addresses = listOf(
      CprAddressDto(
        cprAddressId = "addr-prison-1",
        buildingNumber = "10",
        thoroughfareName = "Prison Road",
        postTown = "Leeds",
        postcode = "LS1 1AA",
        startDate = "2020-04-03",
        comment = null,
        status = CprCodeDescriptionDto(code = "M", description = "Main"),
        usages = listOf(
          CprAddressUsageDto(
            code = "HOME",
            description = "Home",
            isActive = true,
          ),
        ),
        contacts = listOf(
          CprContactDto(
            type = CprCodeDescriptionDto(code = "TELEPHONE", description = "Telephone"),
            value = "01234567890",
          ),
          CprContactDto(
            type = CprCodeDescriptionDto(code = "MOBILE", description = "Mobile"),
            value = "07700900002",
          ),
          CprContactDto(
            type = CprCodeDescriptionDto(code = "EMAIL", description = "Email"),
            value = "john.smith@example.com",
          ),
        ),
      ),
    ),
    identifiers = CprIdentifiersDto(
      crns = emptyList(),
      prisonNumbers = listOf(prisonNumber),
      pncs = listOf("12/394773H"),
      cros = listOf("29906/12J"),
    ),
  )

  fun createCprPrisonPersonAdditionalDetails(): PersonAdditionalDetails = PersonAdditionalDetails(
    ethnicity = "White",
    preferredLanguage = null,
    neurodiverseConditions = null,
    religionOrBelief = "Christian",
    transgender = null,
    sexualOrientation = "Heterosexual",
    genderIdentity = "Male",
    nationalities = listOf("British"),
    address = "10, Prison Road, Leeds, LS1 1AA",
    addressType = "Home",
    addressStartDate = LocalDate.of(2020, 4, 3),
    addressNotes = null,
    phoneNumber = "01234567890",
    mobileNumber = "07700900002",
    emailAddress = "john.smith@example.com",
    disability = false,
  )

  // Generic factory for tests that don't depend on source system (e.g. PersonAggregateMapper tests)
  fun createTestPersonAdditionalDetails(): PersonAdditionalDetails = PersonAdditionalDetails(
    ethnicity = "White",
    preferredLanguage = "English",
    neurodiverseConditions = null,
    religionOrBelief = "Christian",
    transgender = null,
    sexualOrientation = "Heterosexual",
    genderIdentity = "Male",
    nationalities = listOf("Argentine", "Brazilian"),
    address = "1, Test Street, Testville, TE1 1ST",
    addressType = "Friends/Family (settled) (verified)",
    addressStartDate = LocalDate.of(2005, 12, 1),
    addressNotes = "No notes",
    phoneNumber = "01234567890",
    mobileNumber = "07700900002",
    emailAddress = "john.smith@example.com",
    disability = true,
  )

  fun cprProbationPersonJson(crn: String) = createCprProbationPersonDto(crn).toJson()

  fun cprPrisonPersonJson(prisonNumber: String) = createCprPrisonPersonDto(prisonNumber).toJson()

  fun cprPersonNotFoundJson() = """
        {
          "error": "Not Found",
          "status": 404,
          "message": "Person not found"
        }
  """.trimIndent()

  fun createSendEmailResponse(
    notificationId: UUID = UUID.randomUUID(),
    templateId: UUID = UUID.randomUUID(),
    templateVersion: Int = 1,
    templateUri: String = "https://api.notifications.service.gov.uk/templates/$templateId",
    body: String = "Email body",
    subject: String = "Email subject",
    fromEmail: String? = "noreply@example.com",
    reference: String? = "test-reference",
  ): SendEmailResponse {
    val jsonBody = """
        {
            "id": "$notificationId",
            "reference": ${reference?.let { "\"$it\"" } ?: "null"},
            "content": {
                "body": "$body",
                "subject": "$subject",
                "from_email": ${fromEmail?.let { "\"$it\"" } ?: "null"}
            },
            "template": {
                "id": "$templateId",
                "version": $templateVersion,
                "uri": "$templateUri"
            }
        }
    """.trimIndent()
    return SendEmailResponse(jsonBody)
  }
}
