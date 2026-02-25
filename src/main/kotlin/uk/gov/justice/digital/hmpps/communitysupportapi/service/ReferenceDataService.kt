package uk.gov.justice.digital.hmpps.communitysupportapi.service

import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.communitysupportapi.model.ProbationOffice
import uk.gov.justice.digital.hmpps.communitysupportapi.util.CsvFileHelper

@Service
class ReferenceDataService {
  @Volatile
  private var cachedProbationOffices: List<ProbationOffice>? = null

  @Value("\${reference-data.probation-offices.path}")
  private lateinit var probationOfficesPath: String

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  @PostConstruct
  fun init() {
    cachedProbationOffices = loadProbationOffices()
  }

  private fun loadProbationOffices(): List<ProbationOffice> = CsvFileHelper.readFromClasspath(
    probationOfficesPath,
    { record ->
      ProbationOffice(
        probationOfficeId = record.get("probation_office_id")?.toIntOrNull() ?: 0,
        name = record.get("name"),
        address = record.get("address"),
        probationRegionId = record.get("probation_region_id"),
        govUkUrl = record.get("gov_uk_url"),
        deliusCRSLocationId = record.get("delius_crs_location_id"),
      )
    },
  )

  fun getProbationOffices(forceRefresh: Boolean = false): List<ProbationOffice> {
    if (forceRefresh || cachedProbationOffices == null) {
      synchronized(this) {
        cachedProbationOffices = loadProbationOffices()
      }
    }
    return cachedProbationOffices!!
  }
}
