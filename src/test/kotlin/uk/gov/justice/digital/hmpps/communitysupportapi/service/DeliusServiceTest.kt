package uk.gov.justice.digital.hmpps.communitysupportapi.service

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
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

    whenever(deliusClient.getPersonByCrn(CRN)).thenReturn(Mono.just(deliusPersonDto))

    val result = deliusService.getPersonDetailsByCrn(CRN)

    StepVerifier.create(result)
      .expectNextMatches { it.person.identifier == expectedPersonAggregate.person.identifier }
      .verifyComplete()

    verify(deliusClient).getPersonByCrn(CRN)
    verifyNoMoreInteractions(deliusClient)
  }

  @Test
  fun `should emit NotFoundException when Delius client returns empty`() {
    val crn = "X123456"

    whenever(deliusClient.getPersonByCrn(crn)).thenReturn(Mono.empty())

    val result = deliusService.getPersonDetailsByCrn(crn)

    StepVerifier.create(result)
      .expectErrorMatches { it is NotFoundException && it.message == "Person not found in Delius with identifier: $crn" }
      .verify()

    verify(deliusClient).getPersonByCrn(crn)
    verifyNoMoreInteractions(deliusClient)
  }
}
