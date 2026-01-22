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
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Person
import uk.gov.justice.digital.hmpps.communitysupportapi.exception.NotFoundException
import uk.gov.justice.digital.hmpps.communitysupportapi.mapper.toAdditionalDetails
import uk.gov.justice.digital.hmpps.communitysupportapi.mapper.toEntity
import uk.gov.justice.digital.hmpps.communitysupportapi.mapper.toPerson
import uk.gov.justice.digital.hmpps.communitysupportapi.model.PersonAggregate
import uk.gov.justice.digital.hmpps.communitysupportapi.model.PersonIdentifier
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.PersonRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.ExternalApiResponse.CRN
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.ExternalApiResponse.PRISONER_NUMBER
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.ExternalApiResponse.createDeliusPersonDto
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.ExternalApiResponse.createNomisPersonDto
import uk.gov.justice.digital.hmpps.communitysupportapi.validation.PersonIdentifierValidator

@ExtendWith(MockitoExtension::class)
class PersonServiceTest {

  @Mock
  lateinit var personIdentifierValidator: PersonIdentifierValidator

  @Mock
  lateinit var deliusService: DeliusService

  @Mock
  lateinit var nomisService: NomisService

  @Mock
  lateinit var personRepository: PersonRepository

  @InjectMocks
  lateinit var personService: PersonService

  @Test
  fun `Calls Delius service with CRN identifier`() {
    val identifier = PersonIdentifier.Crn(CRN)

    whenever(personIdentifierValidator.validate(CRN)).thenReturn(identifier)

    val deliusPersonDto = createDeliusPersonDto(CRN)

    val expectedPersonAggregate = PersonAggregate(
      person = deliusPersonDto.toPerson(),
      additionalDetails = deliusPersonDto.toAdditionalDetails(),
    )

    whenever(deliusService.getPersonDetailsByCrn(CRN)).thenReturn(Mono.just(expectedPersonAggregate))

    whenever(personRepository.save(any())).thenAnswer { invocation ->
      invocation.arguments[0] as Person
    }

    val result = personService.getPerson(CRN)

    assertEquals(identifier.value, result.personIdentifier)

    verify(deliusService).getPersonDetailsByCrn(CRN)
    verifyNoInteractions(nomisService)
  }

  @Test
  fun `Calls Nomis service with Prisoner Number identifier`() {
    val identifier = PersonIdentifier.PrisonerNumber(PRISONER_NUMBER)

    whenever(personIdentifierValidator.validate(PRISONER_NUMBER)).thenReturn(identifier)

    val nomisPersonDto = createNomisPersonDto(PRISONER_NUMBER)

    val personAggregate = PersonAggregate(
      person = nomisPersonDto.toPerson(),
      additionalDetails = nomisPersonDto.toAdditionalDetails(),
    )

    val person = personAggregate.toEntity()

    whenever(nomisService.getPersonDetailsByPrisonerNumber(PRISONER_NUMBER))
      .thenReturn(Mono.just(personAggregate))

    whenever(personRepository.save(any())).thenAnswer { invocation ->
      invocation.arguments[0] as Person
    }

    val result = personService.getPerson(PRISONER_NUMBER)

    assertEquals(identifier.value, result.personIdentifier)

    verify(nomisService).getPersonDetailsByPrisonerNumber(PRISONER_NUMBER)
    verifyNoInteractions(deliusService)
  }

  @Test
  fun `invalid identifier throws ValidationException`() {
    whenever(personIdentifierValidator.validate("NOT_VALID"))
      .thenThrow(ValidationException("Invalid identifier"))

    assertThrows<ValidationException> {
      personService.getPerson("NOT_VALID")
    }

    verifyNoInteractions(deliusService, nomisService)
  }

  @Test
  fun `person not found from an external api throws NotFoundException`() {
    val crn = "X123456"
    val identifier = PersonIdentifier.Crn(crn)

    whenever(personIdentifierValidator.validate(crn)).thenReturn(identifier)
    whenever(deliusService.getPersonDetailsByCrn(crn))
      .thenReturn(Mono.error(NotFoundException("Person not found in Delius with identifier: $crn")))

    assertThrows<NotFoundException> {
      personService.getPerson(identifier.value)
    }
  }
}
