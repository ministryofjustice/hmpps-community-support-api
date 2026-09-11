package uk.gov.justice.digital.hmpps.communitysupportapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.WithdrawalReason
import java.util.UUID

@Repository
interface WithdrawalReasonRepository : JpaRepository<WithdrawalReason, UUID> {
  fun findAllByOrderByGroupAscNameAsc(): List<WithdrawalReason>

  fun findByName(name: String): WithdrawalReason?
}
