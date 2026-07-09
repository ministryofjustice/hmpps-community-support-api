package uk.gov.justice.digital.hmpps.communitysupportapi.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.communitysupportapi.client.AssessRisksAndNeedsClient
import uk.gov.justice.digital.hmpps.communitysupportapi.exception.NotFoundException
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.ExternalApiResponse.CRN
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.ExternalApiResponse.createArnsRoshRiskDto
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.ExternalApiResponse.createStaleArnsRoshRiskDto
import uk.gov.justice.digital.hmpps.communitysupportapi.util.toFormattedAssessmentDate
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class AssessRisksAndNeedsServiceTest {

  @Mock
  lateinit var assessRisksAndNeedsClient: AssessRisksAndNeedsClient

  @InjectMocks
  lateinit var assessRisksAndNeedsService: AssessRisksAndNeedsService

  @Test
  fun `should return full risk data when assessment is within 12 months`() {
    val recentAssessment = createArnsRoshRiskDto(assessedOn = LocalDateTime.now().minusDays(30))
    whenever(assessRisksAndNeedsClient.getRoshRisksByCrn(CRN)).thenReturn(recentAssessment)

    val result = assessRisksAndNeedsService.getRoshRisksByCrn(CRN)

    assertTrue(result.assessmentWithin12Months)
    assertNotNull(result.riskToSelf)
    assertNotNull(result.summary)
    assertEquals(recentAssessment.assessedOn?.toFormattedAssessmentDate(), result.assessedOn)
    assertEquals("HIGH", result.summary?.overallRiskLevel)
    verify(assessRisksAndNeedsClient).getRoshRisksByCrn(CRN)
  }

  @Test
  fun `should return blank response when assessment is older than 12 months`() {
    val staleAssessment = createStaleArnsRoshRiskDto()
    whenever(assessRisksAndNeedsClient.getRoshRisksByCrn(CRN)).thenReturn(staleAssessment)

    val result = assessRisksAndNeedsService.getRoshRisksByCrn(CRN)

    assertFalse(result.assessmentWithin12Months)
    assertNull(result.riskToSelf)
    assertNull(result.summary)
    assertNull(result.assessedOn)
    verify(assessRisksAndNeedsClient).getRoshRisksByCrn(CRN)
  }

  @Test
  fun `should return blank response when assessedOn is null`() {
    val noDateAssessment = createArnsRoshRiskDto(assessedOn = null)
    whenever(assessRisksAndNeedsClient.getRoshRisksByCrn(CRN)).thenReturn(noDateAssessment)

    val result = assessRisksAndNeedsService.getRoshRisksByCrn(CRN)

    assertFalse(result.assessmentWithin12Months)
    assertNull(result.riskToSelf)
    assertNull(result.summary)
    verify(assessRisksAndNeedsClient).getRoshRisksByCrn(CRN)
  }

  @Test
  fun `should propagate NotFoundException when CRN is not found`() {
    whenever(assessRisksAndNeedsClient.getRoshRisksByCrn(CRN))
      .thenThrow(NotFoundException("ROSH risks not found in Assess Risks and Needs for CRN: $CRN"))

    assertThrows<NotFoundException> {
      assessRisksAndNeedsService.getRoshRisksByCrn(CRN)
    }

    verify(assessRisksAndNeedsClient).getRoshRisksByCrn(CRN)
  }
}
