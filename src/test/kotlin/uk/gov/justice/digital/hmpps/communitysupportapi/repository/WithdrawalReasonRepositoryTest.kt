package uk.gov.justice.digital.hmpps.communitysupportapi.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.IntegrationTestBase

class WithdrawalReasonRepositoryTest : IntegrationTestBase() {

  @Autowired
  private lateinit var withdrawalReasonRepository: WithdrawalReasonRepository

  @Test
  fun `should load seeded withdrawal reasons ordered by group then name`() {
    val reasons = withdrawalReasonRepository.findAllByOrderByGroupAscNameAsc()

    assertThat(reasons.map { it.name to it.group }).containsExactly(
      "Ineligible referral" to "Problem with referral",
      "Mistaken or duplicate referral" to "Problem with referral",
      "Acquitted on appeal" to "Sentence or custody related",
      "Returned to custody" to "Sentence or custody related",
      "Sentence expired" to "Sentence or custody related",
      "Sentence revoked" to "Sentence or custody related",
      "Another reason" to "User related",
      "Died" to "User related",
      "Moved out of service area" to "User related",
      "Needs met through another route" to "User related",
      "Not engaged" to "User related",
      "Work, caring commitments or sickness" to "User related",
    )
  }

  @Test
  fun `existsByName should return true for a seeded withdrawal reason name`() {
    assertThat(withdrawalReasonRepository.existsByName("Sentence expired")).isTrue()
  }

  @Test
  fun `existsByName should return false for an unknown withdrawal reason name`() {
    assertThat(withdrawalReasonRepository.existsByName("Not a real reason")).isFalse()
  }
}
