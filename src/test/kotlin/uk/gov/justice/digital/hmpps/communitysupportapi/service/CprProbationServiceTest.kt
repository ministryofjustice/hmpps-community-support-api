package uk.gov.justice.digital.hmpps.communitysupportapi.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.communitysupportapi.client.CorePersonRecordClient
import uk.gov.justice.digital.hmpps.communitysupportapi.exception.NotFoundException
import uk.gov.justice.digital.hmpps.communitysupportapi.mapper.toAdditionalDetails
import uk.gov.justice.digital.hmpps.communitysupportapi.mapper.toPrisonPerson
import uk.gov.justice.digital.hmpps.communitysupportapi.mapper.toProbationPerson
import uk.gov.justice.digital.hmpps.communitysupportapi.model.PersonAggregate
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.ExternalApiResponse.CRN
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.ExternalApiResponse.PRISONER_NUMBER
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.ExternalApiResponse.createCprPrisonPersonDto
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.ExternalApiResponse.createCprProbationPersonDto

@ExtendWith(MockitoExtension::class)
class CprProbationServiceTest {

  @Mock
  lateinit var corePersonRecordClient: CorePersonRecordClient

  private lateinit var cprProbationService: CprProbationService

  @BeforeEach
  fun setUp() {
    cprProbationService = CprProbationService(corePersonRecordClient = corePersonRecordClient)
  }

  @Test
  fun `should return person details by CRN from Core Person Record`() {
    val cprPersonDto = createCprProbationPersonDto(CRN)
    val expectedPersonAggregate = PersonAggregate(
      person = cprPersonDto.toProbationPerson(),
      additionalDetails = cprPersonDto.toAdditionalDetails(),
    )

    whenever(corePersonRecordClient.getPersonByCrn(CRN)).thenReturn(cprPersonDto)

    val result = cprProbationService.getPersonDetailsByCrn(CRN)

    assertEquals(expectedPersonAggregate.person.identifier, result.person.identifier)

    verify(corePersonRecordClient).getPersonByCrn(CRN)
    verifyNoMoreInteractions(corePersonRecordClient)
  }

  @Test
  fun `should throw NotFoundException when Core Person Record client fails to find person by CRN`() {
    val crn = "X123456"

    whenever(corePersonRecordClient.getPersonByCrn(crn))
      .thenThrow(NotFoundException("Person not found in Core Person Record with CRN: $crn"))

    assertThrows(NotFoundException::class.java) {
      cprProbationService.getPersonDetailsByCrn(crn)
    }

    verify(corePersonRecordClient).getPersonByCrn(crn)
    verifyNoMoreInteractions(corePersonRecordClient)
  }

  @Test
  fun `should return person details by prison number from Core Person Record`() {
    val cprPersonDto = createCprPrisonPersonDto(PRISONER_NUMBER)
    val expectedAggregate = PersonAggregate(
      person = cprPersonDto.toPrisonPerson(),
      additionalDetails = cprPersonDto.toAdditionalDetails(),
    )

    whenever(corePersonRecordClient.getPersonByPrisonNumber(PRISONER_NUMBER)).thenReturn(cprPersonDto)

    val result = cprProbationService.getPersonDetailsByPrisonNumber(PRISONER_NUMBER)

    assertEquals(expectedAggregate.person.identifier, result.person.identifier)

    verify(corePersonRecordClient).getPersonByPrisonNumber(PRISONER_NUMBER)
    verifyNoMoreInteractions(corePersonRecordClient)
  }

  @Test
  fun `should throw NotFoundException when Core Person Record client fails to find person by prison number`() {
    val prisonNumber = "A1234BC"

    whenever(corePersonRecordClient.getPersonByPrisonNumber(prisonNumber))
      .thenThrow(NotFoundException("Person not found in Core Person Record with prison number: $prisonNumber"))

    assertThrows(NotFoundException::class.java) {
      cprProbationService.getPersonDetailsByPrisonNumber(prisonNumber)
    }

    verify(corePersonRecordClient).getPersonByPrisonNumber(prisonNumber)
    verifyNoMoreInteractions(corePersonRecordClient)
  }
}
