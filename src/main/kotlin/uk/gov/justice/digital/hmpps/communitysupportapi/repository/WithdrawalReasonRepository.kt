package uk.gov.justice.digital.hmpps.communitysupportapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferenceDataWithdrawalReason
import java.util.UUID

@Repository
interface WithdrawalReasonRepository : JpaRepository<ReferenceDataWithdrawalReason, UUID> {
  fun findAllByOrderByGroupAscNameAsc(): List<ReferenceDataWithdrawalReason>

  fun findByName(name: String): ReferenceDataWithdrawalReason?
}
