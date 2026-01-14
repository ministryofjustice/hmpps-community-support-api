package uk.gov.justice.digital.hmpps.communitysupportapi.mapper

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.communitysupportapi.model.PersonIdentifier
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.ExternalApiResponse.CRN
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.ExternalApiResponse.createDeliusPersonAdditionalDetails
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.ExternalApiResponse.createDeliusPersonDto

class DeliusPersonMapperTest {

  @Test
  fun `maps Delius Person dto to domain Person`() {
    val deliusPersonDto = createDeliusPersonDto(CRN)
    val deliusPersonIdentifier = PersonIdentifier.Crn(CRN)
    val person = deliusPersonDto.toPerson()

    assertEquals(deliusPersonIdentifier, person.identifier)
  }

  @Test
  fun `maps Delius Person Details dto to domain Person Additional Details`() {
    val expectedDeliusPersonAdditionalDetails = createDeliusPersonAdditionalDetails()
    val deliusPersonDto = createDeliusPersonDto(CRN)
    val personAdditionalDetails = deliusPersonDto.toAdditionalDetails()

    assertEquals(expectedDeliusPersonAdditionalDetails, personAdditionalDetails)
  }
}
