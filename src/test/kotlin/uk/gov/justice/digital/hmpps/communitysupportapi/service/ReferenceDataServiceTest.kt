package uk.gov.justice.digital.hmpps.communitysupportapi.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.communitysupportapi.model.ProbationOffice

class ReferenceDataServiceTest : IntegrationTestBase() {
  @Autowired
  private lateinit var referenceDataService: ReferenceDataService

  val mockProbationOffices: List<ProbationOffice> = listOf(
    ProbationOffice(
      1,
      "Derby: Derwent Centre",
      "Derwent Centre, 1 Stuart Street, Derby, DE1 2EQ",
      "F",
      "https://www.gov.uk/guidance/derby-derwent-centre",
    ),
    ProbationOffice(
      5,
      "Leicestershire: Coalville Probation Office",
      "Probation Office, 27 London Road, Coalville, Leicestershire, LE67 3JB",
      "F",
      "https://www.gov.uk/guidance/leicestershire-coalville-probation-office",
      "CRS0086",
    ),
    ProbationOffice(
      probationOfficeId = 128,
      name = "Warrington: Warrington Probation Office",
      address = "Units 3 & 4 Bankside, Crosfield Street, Warrington, WA1 1UP",
      probationRegionId = "B",
      deliusCRSLocationId = "CRS0328",
    ),
  )

  @Test
  fun `should load Probation Offices`() {
    val probationOffices = referenceDataService.getProbationOffices()

    assertThat(probationOffices).isNotEmpty
    assertEquals(mockProbationOffices[0], probationOffices[0])
    assertEquals(mockProbationOffices[1], probationOffices[4])
    assertEquals(mockProbationOffices[2], probationOffices[127])
  }
}
