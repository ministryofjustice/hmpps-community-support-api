package uk.gov.justice.digital.hmpps.communitysupportapi.mapper

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.communitysupportapi.model.Person
import uk.gov.justice.digital.hmpps.communitysupportapi.model.PersonIdentifier

class PersonMapperTest {

  @Test
  fun `maps domain Person to a Response Person dto`() {
    val crnValue = "X123456"
    val person = Person(identifier = PersonIdentifier.Crn(crnValue))
    val personDto = person.toDto()

    assertEquals(crnValue, personDto.personIdentifier)
  }
}
