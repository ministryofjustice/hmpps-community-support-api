package uk.gov.justice.digital.hmpps.communitysupportapi.testdata

import uk.gov.justice.digital.hmpps.communitysupportapi.dto.nomis.ContactDetails
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.nomis.NomisPersonDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.nomis.OffenderProfile
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.nomis.PhoneNumber
import uk.gov.justice.digital.hmpps.communitysupportapi.util.toJson

object ExternalApiResponse {

  fun nomisPerson(prisonerNumber: String): NomisPersonDto {
    return NomisPersonDto(
      prisonerNumber = prisonerNumber,
      firstName = "John",
      lastName = "Smith",
      dateOfBirth = "1985-01-01",
      contactDetails = ContactDetails(
        phoneNumbers = listOf(PhoneNumber("01141234567", "HOME"))
      ),
      offenderProfile = OffenderProfile()
    )
  }

  fun nomisPersonJson(prisonerNumber: String) =
    nomisPerson(prisonerNumber).toJson()

  fun nomisPersonNotFoundJson() = """
        {
          "error": "Not Found",
          "status": 404,
          "message": "Prisoner not found"
        }
    """.trimIndent()
}
