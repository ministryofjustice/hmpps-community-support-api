package uk.gov.justice.digital.hmpps.communitysupportapi.mapper

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.communitysupportapi.model.PersonIdentifier
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.ExternalApiResponse

class NomisPersonMapperTest {

  @Test
  fun `maps Nomis Person dto to domain Person`() {
    val prisonerNumber = "A1234BC"
    val nomisPersonDto = ExternalApiResponse.nomisPerson(prisonerNumber)
    val nomisPersonIdentifier = PersonIdentifier.PrisonerNumber(nomisPersonDto.prisonerNumber)

    val person = nomisPersonDto.toDomain()

    assertEquals(nomisPersonIdentifier, person.identifier)
  }
}
