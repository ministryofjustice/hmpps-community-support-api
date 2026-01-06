package uk.gov.justice.digital.hmpps.communitysupportapi.mapper

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.DeliusPersonDto
import uk.gov.justice.digital.hmpps.communitysupportapi.model.PersonIdentifier

class DeliusPersonMapperTest {

  @Test
  fun `maps Delius Person dto to domain Person`() {
    val crnValue = "X123456"
    val deliusPersonDto = DeliusPersonDto(crn = crnValue)
    val deliusPersonIdentifier = PersonIdentifier.Crn(crnValue)

    val person = deliusPersonDto.toDomain()

    assertEquals(deliusPersonIdentifier, person.identifier)
  }
}
