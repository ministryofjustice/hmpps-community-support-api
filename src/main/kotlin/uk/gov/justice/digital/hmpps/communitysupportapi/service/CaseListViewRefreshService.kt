package uk.gov.justice.digital.hmpps.communitysupportapi.service

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CaseListViewRefreshService(
  @PersistenceContext private val entityManager: EntityManager,
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  @Scheduled(fixedRateString = $$"${case-list-view.refresh-rate-ms:300000}")
  @Transactional
  fun refreshMaterializedView() {
    log.info("Refreshing case_list_view materialized view")
    try {
      entityManager.createNativeQuery("REFRESH MATERIALIZED VIEW CONCURRENTLY case_list_view").executeUpdate()
      log.info("Successfully refreshed case_list_view materialized view")
    } catch (e: Exception) {
      log.error("Failed to refresh case_list_view materialized view", e)
      throw e
    }
  }
}
