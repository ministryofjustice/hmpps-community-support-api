package uk.gov.justice.digital.hmpps.communitysupportapi.mapper

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.communitysupportapi.model.PersonIdentifier
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.ExternalApiResponse.PRISONER_NUMBER
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.ExternalApiResponse.createNomisPersonAdditionalDetails
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.ExternalApiResponse.createNomisPersonDto

class NomisPersonMapperTest {

  @Test
  fun `maps Nomis Person dto to domain Person`() {
    val nomisPersonDto = createNomisPersonDto(PRISONER_NUMBER)
    val nomisPersonIdentifier = PersonIdentifier.PrisonerNumber(PRISONER_NUMBER)
    val person = nomisPersonDto.toPerson()

    assertEquals(nomisPersonIdentifier, person.identifier)
  }

  @Test
  fun `maps Nomis Person Details dto to domain Person Additional Details`() {
    val expectedNomisPersonAdditionalDetails = createNomisPersonAdditionalDetails()
    val nomisPersonDto = createNomisPersonDto(PRISONER_NUMBER)
    val personAdditionalDetails = nomisPersonDto.toAdditionalDetails()

    assertEquals(expectedNomisPersonAdditionalDetails, personAdditionalDetails)
  }
}
