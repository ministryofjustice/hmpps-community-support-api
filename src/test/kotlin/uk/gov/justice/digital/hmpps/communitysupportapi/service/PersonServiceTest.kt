package uk.gov.justice.digital.hmpps.communitysupportapi.service

import jakarta.validation.ValidationException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.communitysupportapi.validation.PersonIdentifierValidator
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.DeliusPersonDto
import uk.gov.justice.digital.hmpps.communitysupportapi.exception.NotFoundException
import uk.gov.justice.digital.hmpps.communitysupportapi.model.PersonIdentifier
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.ExternalApiResponse

@ExtendWith(MockitoExtension::class)
class PersonServiceTest {

  @Mock
  lateinit var personIdentifierValidator: PersonIdentifierValidator

  @Mock
  lateinit var deliusService: DeliusService

  @Mock
  lateinit var nomisService: NomisService

  @InjectMocks
  lateinit var personService: PersonService

  @Test
  fun `CRN identifier calls Delius service`() {
    val crnValue = "X123456"
    val identifier = PersonIdentifier.Crn(crnValue)

    whenever(personIdentifierValidator.validate(crnValue)).thenReturn(identifier)

    val deliusDto = DeliusPersonDto(crn = crnValue)

    whenever(deliusService.getPersonDetailsByCrn(crnValue))
      .thenReturn(deliusDto)

    val result = personService.getPerson(crnValue)

    assertEquals(identifier, result.identifier)

    verify(deliusService).getPersonDetailsByCrn(crnValue)
    verifyNoInteractions(nomisService)
  }

  @Test
  fun `Prisoner Number identifier calls Nomis service`() {
    val prisonerNumber = "A1234BC"
    val identifier = PersonIdentifier.PrisonerNumber(prisonerNumber)

    whenever(personIdentifierValidator.validate(prisonerNumber)).thenReturn(identifier)

    val nomisPersonDto = ExternalApiResponse.nomisPerson(prisonerNumber)

    whenever(nomisService.getPersonDetailsByPrisonerNumber(prisonerNumber))
      .thenReturn(nomisPersonDto)

    val result = personService.getPerson(prisonerNumber)

    verify(nomisService).getPersonDetailsByPrisonerNumber(prisonerNumber)
    verifyNoInteractions(deliusService)

    assertEquals(identifier, result.identifier)
  }

  @Test
  fun `invalid identifier throws ValidationException`() {
    whenever(personIdentifierValidator.validate("BAD"))
      .thenThrow(ValidationException("Invalid identifier"))

    assertThrows<ValidationException> {
      personService.getPerson("BAD")
    }

    verifyNoInteractions(deliusService, nomisService)
  }

  @Test
  fun `CRN not found in Delius throws NotFoundException`() {
    val crnValue = "X123456"
    val identifier = PersonIdentifier.Crn(crnValue)

    whenever(personIdentifierValidator.validate(crnValue)).thenReturn(identifier)
    whenever(deliusService.getPersonDetailsByCrn(crnValue)).thenReturn(null)

    assertThrows<NotFoundException> {
      personService.getPerson(crnValue)
    }
  }
}
