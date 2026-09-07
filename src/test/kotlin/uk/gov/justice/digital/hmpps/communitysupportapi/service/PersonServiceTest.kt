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
import uk.gov.justice.digital.hmpps.communitysupportapi.exception.NotFoundException
import uk.gov.justice.digital.hmpps.communitysupportapi.mapper.toAdditionalDetails
import uk.gov.justice.digital.hmpps.communitysupportapi.mapper.toPrisonPerson
import uk.gov.justice.digital.hmpps.communitysupportapi.mapper.toProbationPerson
import uk.gov.justice.digital.hmpps.communitysupportapi.model.PersonAggregate
import uk.gov.justice.digital.hmpps.communitysupportapi.model.PersonDetailsAndCircumstances
import uk.gov.justice.digital.hmpps.communitysupportapi.model.PersonIdentifier
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.ExternalApiResponse.CRN
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.ExternalApiResponse.PRISONER_NUMBER
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.ExternalApiResponse.createCprPrisonPersonDto
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.ExternalApiResponse.createCprProbationPersonDto
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.ExternalApiResponse.createHomeOfficeInterestDto
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.ExternalApiResponse.createPersonDetailsAndCircumstancesDto
import uk.gov.justice.digital.hmpps.communitysupportapi.validation.PersonIdentifierValidator

@ExtendWith(MockitoExtension::class)
class PersonServiceTest {

  @Mock
  lateinit var personIdentifierValidator: PersonIdentifierValidator

  @Mock
  lateinit var cprProbationService: CprProbationService

  @Mock
  lateinit var nDeliusService: NDeliusService

  @InjectMocks
  lateinit var personService: PersonService

  @Test
  fun `Calls CprProbationService with CRN identifier`() {
    val identifier = PersonIdentifier.Crn(CRN)

    whenever(personIdentifierValidator.validate(CRN)).thenReturn(identifier)

    val cprProbationPersonDto = createCprProbationPersonDto(CRN)

    val expectedPersonAggregate = PersonAggregate(
      person = cprProbationPersonDto.toProbationPerson(),
      additionalDetails = cprProbationPersonDto.toAdditionalDetails(),
    )

    whenever(cprProbationService.getPersonDetailsByCrn(CRN)).thenReturn(expectedPersonAggregate)

    val expectedPersonDetailsAndCircumstances = PersonDetailsAndCircumstances.from(createPersonDetailsAndCircumstancesDto(), createHomeOfficeInterestDto())

    whenever(nDeliusService.getPersonalDetailsAndCircumstancesByIdentifier(CRN)).thenReturn(expectedPersonDetailsAndCircumstances)

    val result = personService.getPerson(CRN)

    assertEquals(identifier.value, result.personIdentifier)

    verify(cprProbationService).getPersonDetailsByCrn(CRN)
  }

  @Test
  fun `Calls CprProbationService with Prisoner Number identifier`() {
    val identifier = PersonIdentifier.PrisonerNumber(PRISONER_NUMBER)

    whenever(personIdentifierValidator.validate(PRISONER_NUMBER)).thenReturn(identifier)

    val cprPrisonPersonDto = createCprPrisonPersonDto(PRISONER_NUMBER)

    val personAggregate = PersonAggregate(
      person = cprPrisonPersonDto.toPrisonPerson(),
      additionalDetails = cprPrisonPersonDto.toAdditionalDetails(),
    )

    val knownCRN = personAggregate.person.knownCrns.first()

    whenever(cprProbationService.getPersonDetailsByPrisonNumber(PRISONER_NUMBER))
      .thenReturn(personAggregate)

    val expectedPersonDetailsAndCircumstances = PersonDetailsAndCircumstances.from(createPersonDetailsAndCircumstancesDto(), createHomeOfficeInterestDto())

    whenever(nDeliusService.getPersonalDetailsAndCircumstancesByIdentifier(knownCRN)).thenReturn(expectedPersonDetailsAndCircumstances)

    val result = personService.getPerson(PRISONER_NUMBER)

    assertEquals(identifier.value, result.personIdentifier)

    verify(cprProbationService).getPersonDetailsByPrisonNumber(PRISONER_NUMBER)
  }

  @Test
  fun `invalid identifier throws ValidationException`() {
    whenever(personIdentifierValidator.validate("NOT_VALID"))
      .thenThrow(ValidationException("Invalid identifier"))

    assertThrows<ValidationException> {
      personService.getPerson("NOT_VALID")
    }

    verifyNoInteractions(cprProbationService, nDeliusService)
  }

  @Test
  fun `person not found from CPR throws NotFoundException`() {
    val crn = "X123456"
    val identifier = PersonIdentifier.Crn(crn)

    whenever(personIdentifierValidator.validate(crn)).thenReturn(identifier)
    whenever(cprProbationService.getPersonDetailsByCrn(crn))
      .thenThrow(NotFoundException("Person not found in Core Person Record with CRN: $crn"))

    assertThrows<NotFoundException> {
      personService.getPerson(identifier.value)
    }

    verifyNoInteractions(nDeliusService)
  }

  @Test
  fun `person not found from nDelius throws NotFoundException`() {
    val crn = "X123456"
    val identifier = PersonIdentifier.Crn(crn)

    whenever(personIdentifierValidator.validate(crn)).thenReturn(identifier)

    val cprProbationPersonDto = createCprProbationPersonDto(crn)

    val personAggregate = PersonAggregate(
      person = cprProbationPersonDto.toProbationPerson(),
      additionalDetails = cprProbationPersonDto.toAdditionalDetails(),
    )

    whenever(personIdentifierValidator.validate(crn)).thenReturn(identifier)
    whenever(cprProbationService.getPersonDetailsByCrn(crn)).thenReturn(personAggregate)
    whenever(nDeliusService.getPersonalDetailsAndCircumstancesByIdentifier(crn)).thenThrow(NotFoundException("Person not found in nDelius with CRN: $crn"))

    assertThrows<NotFoundException> {
      personService.getPerson(identifier.value)
    }
  }
}
