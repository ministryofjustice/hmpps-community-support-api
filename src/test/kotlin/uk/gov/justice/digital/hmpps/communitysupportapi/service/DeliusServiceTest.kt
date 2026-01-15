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
import uk.gov.justice.digital.hmpps.communitysupportapi.client.DeliusClient
import uk.gov.justice.digital.hmpps.communitysupportapi.exception.NotFoundException
import uk.gov.justice.digital.hmpps.communitysupportapi.mapper.toAdditionalDetails
import uk.gov.justice.digital.hmpps.communitysupportapi.mapper.toPerson
import uk.gov.justice.digital.hmpps.communitysupportapi.model.PersonAggregate
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.ExternalApiResponse.CRN
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.ExternalApiResponse.createDeliusPersonDto

@ExtendWith(MockitoExtension::class)
class DeliusServiceTest {

  @Mock
  lateinit var deliusClient: DeliusClient

  private lateinit var deliusService: DeliusService

  @BeforeEach
  fun setUp() {
    deliusService = DeliusService(deliusClient = deliusClient)
  }

  @Test
  fun `should build correct URI and return person details by CRN`() {
    val deliusPersonDto = createDeliusPersonDto(CRN)

    whenever(deliusClient.getPersonByCrn(CRN))
      .thenReturn(deliusPersonDto)

    val personAggregate = PersonAggregate(
      person = deliusPersonDto.toPerson(),
      additionalDetails = deliusPersonDto.toAdditionalDetails(),
    )

    val result = deliusService.getPersonDetailsByCrn(CRN)

    assertThat(result).isEqualTo(personAggregate)

    verify(deliusClient).getPersonByCrn(CRN)
    verifyNoMoreInteractions(deliusClient)
  }

  @Test
  fun `should return null when Delius client returns null`() {
    val crn = "X123456"

    whenever(deliusClient.getPersonByCrn(crn)).thenReturn(null)

    val exception = assertThrows<NotFoundException> {
      deliusService.getPersonDetailsByCrn(crn)
    }

    assertThat(exception.message).isEqualTo("Person not found in Delius with identifier: $crn")

    verify(deliusClient).getPersonByCrn(crn)
    verifyNoMoreInteractions(deliusClient)
  }
}
