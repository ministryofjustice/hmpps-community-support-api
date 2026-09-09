package uk.gov.justice.digital.hmpps.communitysupportapi.repository

import jakarta.persistence.EntityManager
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.OutcomeSetting
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.NeedFactory
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.OutcomeFactory

class OutcomeRepositoryTest : IntegrationTestBase() {

  @Autowired
  private lateinit var needRepository: NeedRepository

  @Autowired
  private lateinit var outcomeRepository: OutcomeRepository

  @Autowired
  private lateinit var entityManager: EntityManager

  @Test
  @Transactional
  fun `should load outcomes through need relationship in order`() {
    val need = needRepository.saveAndFlush(
      NeedFactory()
        .withLabel("Accommodation")
        .withOrderNumber(999)
        .create(),
    )

    outcomeRepository.saveAllAndFlush(
      listOf(
        OutcomeFactory()
          .withNeedId(need.id)
          .withOrderNumber(2)
          .withText("Second outcome")
          .withSetting(OutcomeSetting.COMMUNITY)
          .create(),
        OutcomeFactory()
          .withNeedId(need.id)
          .withOrderNumber(1)
          .withText("First outcome")
          .withSetting(OutcomeSetting.CUSTODY)
          .create(),
      ),
    )

    entityManager.clear()

    val loadedNeed = needRepository.findById(need.id).orElseThrow()
    val outcomes = loadedNeed.outcomes

    assertEquals(2, outcomes.size)
    assertEquals("First outcome", outcomes[0].text)
    assertEquals(1, outcomes[0].orderNumber)
    assertEquals(OutcomeSetting.CUSTODY, outcomes[0].setting)
    assertEquals("Second outcome", outcomes[1].text)
    assertEquals(2, outcomes[1].orderNumber)
    assertEquals(OutcomeSetting.COMMUNITY, outcomes[1].setting)

    assertEquals(
      listOf(1, 2),
      outcomeRepository.findAllByNeedIdOrderByOrderNumberAsc(need.id).map { it.orderNumber },
    )
  }
}
