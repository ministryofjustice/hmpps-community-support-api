package uk.gov.justice.digital.hmpps.communitysupportapi.common

import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Transactional
@Component
class TestDataCleaner(
  @Autowired
  private val entityManager: EntityManager,
) {
  fun cleanAllTables() {
    entityManager.apply {
      createNativeQuery("TRUNCATE TABLE referral CASCADE").executeUpdate()
      createNativeQuery("TRUNCATE TABLE person_additional_details CASCADE").executeUpdate()
      createNativeQuery("TRUNCATE TABLE person CASCADE").executeUpdate()
    }
  }

  fun refreshMaterializedView() {
    entityManager.createNativeQuery("REFRESH MATERIALIZED VIEW case_list_view").executeUpdate()
  }
}
