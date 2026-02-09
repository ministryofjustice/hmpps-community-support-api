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
    val expectedPersonAggregate = PersonAggregate(
      person = deliusPersonDto.toPerson(),
      additionalDetails = deliusPersonDto.toAdditionalDetails(),
    )

    whenever(deliusClient.getPersonByCrn(CRN)).thenReturn(deliusPersonDto)

    val result = deliusService.getPersonDetailsByCrn(CRN)

    assertEquals(expectedPersonAggregate.person.identifier, result.person.identifier)

    verify(deliusClient).getPersonByCrn(CRN)
    verifyNoMoreInteractions(deliusClient)
  }

  @Test
  fun `should throw NotFoundException when Delius client fails to find person`() {
    val crn = "X123456"

    whenever(deliusClient.getPersonByCrn(crn)).thenThrow(NotFoundException("Person not found in Delius with CRN: $crn"))

    assertThrows(NotFoundException::class.java) {
      deliusService.getPersonDetailsByCrn(crn)
    }

    verify(deliusClient).getPersonByCrn(crn)
    verifyNoMoreInteractions(deliusClient)
  }
}
