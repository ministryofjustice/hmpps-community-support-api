package uk.gov.justice.digital.hmpps.communitysupportapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.WithdrawalReasonRepository
import java.util.UUID

@Service
class WithdrawalReasonService(
  private val withdrawalReasonRepository: WithdrawalReasonRepository,
) {
  fun getWithdrawalReasons(): Map<String, List<String>> = withdrawalReasonRepository.findAllByOrderByGroupAscNameAsc()
    .groupBy({ it.group }, { it.name })

  fun findReasonIdByName(name: String): UUID? = withdrawalReasonRepository.findByName(name)?.id
}
