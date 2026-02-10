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
import uk.gov.justice.digital.hmpps.communitysupportapi.client.NomisClient
import uk.gov.justice.digital.hmpps.communitysupportapi.exception.NotFoundException
import uk.gov.justice.digital.hmpps.communitysupportapi.mapper.toAdditionalDetails
import uk.gov.justice.digital.hmpps.communitysupportapi.mapper.toPerson
import uk.gov.justice.digital.hmpps.communitysupportapi.model.PersonAggregate
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.ExternalApiResponse.PRISONER_NUMBER
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.ExternalApiResponse.createNomisPersonDto

@ExtendWith(MockitoExtension::class)
class NomisServiceTest {

  @Mock
  lateinit var nomisClient: NomisClient

  private lateinit var nomisService: NomisService

  @BeforeEach
  fun setUp() {
    nomisService = NomisService(nomisClient = nomisClient)
  }

  @Test
  fun `should build correct URI and return person details by prisoner number`() {
    val nomisPersonDto = createNomisPersonDto(PRISONER_NUMBER)
    val expectedAggregate = PersonAggregate(
      person = nomisPersonDto.toPerson(),
      additionalDetails = nomisPersonDto.toAdditionalDetails(),
    )

    whenever(nomisClient.getPersonByPrisonerNumber(PRISONER_NUMBER)).thenReturn(nomisPersonDto)

    val result = nomisService.getPersonDetailsByPrisonerNumber(PRISONER_NUMBER)

    assertEquals(expectedAggregate.person.identifier, result.person.identifier)

    verify(nomisClient).getPersonByPrisonerNumber(PRISONER_NUMBER)
    verifyNoMoreInteractions(nomisClient)
  }

  @Test
  fun `should throw NotFoundException when Nomis client fails to find person`() {
    val prisonerNumber = "A1234BC"

    whenever(nomisClient.getPersonByPrisonerNumber(prisonerNumber)).thenThrow(NotFoundException("Person not found in Nomis with identifier: $prisonerNumber"))

    assertThrows(NotFoundException::class.java) {
      nomisService.getPersonDetailsByPrisonerNumber(prisonerNumber)
    }

    verify(nomisClient).getPersonByPrisonerNumber(prisonerNumber)
    verifyNoMoreInteractions(nomisClient)
  }
}
