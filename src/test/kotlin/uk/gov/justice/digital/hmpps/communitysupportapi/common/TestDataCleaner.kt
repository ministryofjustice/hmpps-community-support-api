package uk.gov.justice.digital.hmpps.communitysupportapi.common

import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import kotlin.apply

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
}
