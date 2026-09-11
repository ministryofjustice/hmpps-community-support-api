package uk.gov.justice.digital.hmpps.communitysupportapi.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.WithdrawalReason
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.WithdrawalReasonRepository
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class WithdrawalReasonServiceTest {

  @Mock
  lateinit var withdrawalReasonRepository: WithdrawalReasonRepository

  private lateinit var withdrawalReasonService: WithdrawalReasonService

  @BeforeEach
  fun setup() {
    withdrawalReasonService = WithdrawalReasonService(withdrawalReasonRepository)
  }

  private fun aWithdrawalReason(name: String, group: String) = WithdrawalReason(id = UUID.randomUUID(), name = name, group = group)

  @Test
  fun `getWithdrawalReasonsGroupedByGroupName should group reason names by group name preserving order`() {
    val reasons = listOf(
      aWithdrawalReason("Ineligible referral", "Problem with referral"),
      aWithdrawalReason("Mistaken or duplicate referral", "Problem with referral"),
      aWithdrawalReason("Acquitted on appeal", "Sentence or custody related"),
      aWithdrawalReason("Returned to custody", "Sentence or custody related"),
      aWithdrawalReason("Died", "User related"),
    )
    whenever(withdrawalReasonRepository.findAllByOrderByGroupAscNameAsc()).thenReturn(reasons)

    val result = withdrawalReasonService.getWithdrawalReasons()

    assertEquals(
      mapOf(
        "Problem with referral" to listOf("Ineligible referral", "Mistaken or duplicate referral"),
        "Sentence or custody related" to listOf("Acquitted on appeal", "Returned to custody"),
        "User related" to listOf("Died"),
      ),
      result,
    )
    assertEquals(listOf("Problem with referral", "Sentence or custody related", "User related"), result.keys.toList())
  }

  @Test
  fun `getWithdrawalReasonsGroupedByGroupName should return empty map when there are no withdrawal reasons`() {
    whenever(withdrawalReasonRepository.findAllByOrderByGroupAscNameAsc()).thenReturn(emptyList())

    val result = withdrawalReasonService.getWithdrawalReasons()

    assertEquals(emptyMap<String, List<String>>(), result)
  }

  @Test
  fun `findReasonIdByName should return the id when the repository has a matching reason name`() {
    val reason = aWithdrawalReason("Sentence expired", "Sentence or custody related")
    whenever(withdrawalReasonRepository.findByName("Sentence expired")).thenReturn(reason)

    assertEquals(reason.id, withdrawalReasonService.findReasonIdByName("Sentence expired"))
  }

  @Test
  fun `findReasonIdByName should return null when the repository has no matching reason name`() {
    whenever(withdrawalReasonRepository.findByName("Not a real reason")).thenReturn(null)

    assertNull(withdrawalReasonService.findReasonIdByName("Not a real reason"))
  }
}
