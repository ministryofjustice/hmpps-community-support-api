package uk.gov.justice.digital.hmpps.communitysupportapi.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
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
  fun `should build correct URI and return person details by Prisoner Number`() {
    val nomisPersonDto = createNomisPersonDto(PRISONER_NUMBER)

    whenever(nomisClient.getPersonByPrisonerNumber(PRISONER_NUMBER))
      .thenReturn(nomisPersonDto)

    val personAggregate = PersonAggregate(
      person = nomisPersonDto.toPerson(),
      additionalDetails = nomisPersonDto.toAdditionalDetails(),
    )

    val result = nomisService.getPersonDetailsByPrisonerNumber(PRISONER_NUMBER)

    assertThat(result).isEqualTo(personAggregate)

    verify(nomisClient).getPersonByPrisonerNumber(PRISONER_NUMBER)
    verifyNoMoreInteractions(nomisClient)
  }

  @Test
  fun `should throw NotFoundException when Nomis client returns null`() {
    val prisonerNumber = "Z9876YX"

    whenever(nomisClient.getPersonByPrisonerNumber(prisonerNumber)).thenReturn(null)

    val exception = assertThrows<NotFoundException> {
      nomisService.getPersonDetailsByPrisonerNumber(prisonerNumber)
    }

    assertThat(exception.message).isEqualTo("Person not found in Nomis with identifier: $prisonerNumber")

    verify(nomisClient).getPersonByPrisonerNumber(prisonerNumber)
    verifyNoMoreInteractions(nomisClient)
  }
}
