package uk.gov.justice.digital.hmpps.communitysupportapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralUserAssignment
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralUserAssignmentId
import java.time.OffsetDateTime
import java.util.UUID

interface ReferralUserAssignmentRepository : JpaRepository<ReferralUserAssignment, ReferralUserAssignmentId> {
  @Query(
    """
    SELECT a
    FROM ReferralUserAssignment a 
    WHERE a.id.referralId = :referralId AND a.id.userId = :user_id
    """,
  )
  fun findAllByReferralIdAndUserId(
    @Param("referralId") referralId: UUID,
    @Param("userId") userId: UUID,
  ): List<ReferralUserAssignment>

  @Query(
    """
    SELECT a
    FROM ReferralUserAssignment a 
    WHERE a.id.referralId = :referralId
    """,
  )
  fun findAllByReferralId(
    @Param("referralId") referralId: UUID,
  ): List<ReferralUserAssignment>

  @Query(
    """
    SELECT a
    FROM ReferralUserAssignment a 
    WHERE a.id.referralId = :referralId AND a.id.userId = :user_id
        AND (a.deletedAt IS NULL or a.deletedAt IS NULL)
    """,
  )
  fun findByReferralIdAndUserId(
    @Param("referralId") referralId: UUID,
    @Param("userId") userId: UUID,
  ): List<ReferralUserAssignment>

  @Query(
    """
    SELECT a
    FROM ReferralUserAssignment a 
    WHERE a.id.referralId = :referralId
        AND (a.deletedAt IS NULL or a.deletedAt IS NULL)
    """,
  )
  fun findByReferralId(
    @Param("referralId") referralId: UUID,
  ): List<ReferralUserAssignment>

  @Query(
    """
    SELECT a
    FROM ReferralUserAssignment a 
    WHERE a.id.referralId = :referralId AND a.id.userId = :user_id
        AND a.deletedAt IS NOT NULL AND a.deletedAt IS NOT NULL
    """,
  )
  fun findDeletedByReferralIdAndUserId(
    @Param("referralId") referralId: UUID,
    @Param("userId") userId: UUID,
  ): List<ReferralUserAssignment>

  @Query(
    """
    SELECT a
    FROM ReferralUserAssignment a 
    WHERE a.id.referralId = :referralId
        AND a.deletedAt IS NOT NULL AND a.deletedAt IS NOT NULL
    """,
  )
  fun findDeletedByReferralId(
    @Param("referralId") referralId: UUID,
  ): List<ReferralUserAssignment>

  @Modifying
  @Query(
    """
    UPDATE ReferralUserAssignment a
    SET a.createdBy = :createdBy, a.createdAt = :createdAt, a.deletedBy = null, a.deletedAt = null
    WHERE a.id.referralId = :referralId AND a.id.userId = :user_id
    """,
  )
  fun updateByReferralIdAndUserId(
    @Param("referralId") referralId: UUID,
    @Param("userId") userId: UUID,
    @Param("createdBy") createdBy: String,
    @Param("createdAt") createdAt: OffsetDateTime,
  ): List<ReferralUserAssignment>

  @Modifying
  @Query(
    """
    UPDATE ReferralUserAssignment a
    SET a.deletedBy = :deletedBy, a.deletedAt = :deletedAt
    WHERE a.id.referralId = :referralId AND a.id.userId = :user_id
    """,
  )
  fun markDeletedByReferralIdAndUserId(
    @Param("referralId") referralId: UUID,
    @Param("userId") userId: UUID,
    @Param("deletedBy") deletedBy: String,
    @Param("deletedAt") deletedAt: OffsetDateTime,
  ): List<ReferralUserAssignment>
}
