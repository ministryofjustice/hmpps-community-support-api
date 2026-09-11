package uk.gov.justice.digital.hmpps.communitysupportapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.WithdrawalReasonRepository

@Service
class WithdrawalReasonService(
  private val withdrawalReasonRepository: WithdrawalReasonRepository,
) {
  fun getWithdrawalReasons(): Map<String, List<String>> = withdrawalReasonRepository.findAllByOrderByGroupAscNameAsc()
    .groupBy({ it.group }, { it.name })

  fun isValidReasonName(name: String): Boolean = withdrawalReasonRepository.existsByName(name)
}
