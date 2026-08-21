package uk.gov.justice.digital.hmpps.communitysupportapi.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.communitysupportapi.client.NDeliusClient
import uk.gov.justice.digital.hmpps.communitysupportapi.exception.NotFoundException
import uk.gov.justice.digital.hmpps.communitysupportapi.model.PersonDetailsAndCircumstances
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.ExternalApiResponse.CRN
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.ExternalApiResponse.createHomeOfficeInterestDto
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.ExternalApiResponse.createPersonDetailsAndCircumstancesDto

@ExtendWith(MockitoExtension::class)
class NDeliusServiceTest {

  @Mock
  lateinit var nDeliusClient: NDeliusClient

  private lateinit var nDeliusService: NDeliusService

  @BeforeEach
  fun setup() {
    nDeliusService = NDeliusService(nDeliusClient)
  }

  @Test
  fun `should return person circumstances from nDelius`() {
    val personDetailsAndCircumstancesDto = createPersonDetailsAndCircumstancesDto()
    val homeOfficeInterest = createHomeOfficeInterestDto()
    val expectedPersonCircumstances = PersonDetailsAndCircumstances.from(personDetailsAndCircumstancesDto, homeOfficeInterest)

    whenever(nDeliusClient.getPersonDetailsAndCircumstancesByCrn(CRN)).thenReturn(personDetailsAndCircumstancesDto)
    whenever(nDeliusClient.getHomeOfficeInterestByCrn(CRN)).thenReturn(homeOfficeInterest)

    val result = nDeliusService.getPersonDetailsAndCircumstancesByIdentifier(CRN)

    assertEquals(expectedPersonCircumstances, result)

    verify(nDeliusClient).getPersonDetailsAndCircumstancesByCrn(CRN)
    verify(nDeliusClient).getHomeOfficeInterestByCrn(CRN)
    verifyNoMoreInteractions(nDeliusClient)
  }

  @Test
  fun `should throw NotFoundException when nDelius fails to find person by CRN`() {
    val crn = "X123456"

    whenever(nDeliusClient.getPersonDetailsAndCircumstancesByCrn(crn)).thenThrow(NotFoundException("Person not found in nDelius with CRN: $crn"))

    assertThrows(NotFoundException::class.java) {
      nDeliusService.getPersonDetailsAndCircumstancesByIdentifier(crn)
    }

    verify(nDeliusClient).getPersonDetailsAndCircumstancesByCrn(crn)
    verifyNoMoreInteractions(nDeliusClient)
  }
}
