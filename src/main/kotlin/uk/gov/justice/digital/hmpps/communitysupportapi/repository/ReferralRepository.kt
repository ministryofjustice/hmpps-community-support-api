package uk.gov.justice.digital.hmpps.communitysupportapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Referral
import java.util.UUID

interface ReferralRepository : JpaRepository<Referral, UUID> {
  fun existsByReferenceNumber(reference: String): Boolean
  fun findByReferenceNumber(referenceNumber: String): MutableList<Referral>

  @Query("SELECT r FROM Referral r WHERE r.referenceNumber = :referenceNumber")
  fun findReferenceNumberOrNull(referenceNumber: String): Referral?
}
