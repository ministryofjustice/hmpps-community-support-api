package uk.gov.justice.digital.hmpps.communitysupportapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralUserAssignment
import java.time.LocalDateTime
import java.util.UUID

interface ReferralUserAssignmentRepository : JpaRepository<ReferralUserAssignment, UUID> {
  @Query(
    """
    SELECT a
    FROM ReferralUserAssignment a 
    WHERE a.referral.id = :referralId AND a.user.Id = :userId
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
    WHERE a.referral.id = :referralId
    """,
  )
  fun findAllByReferralId(
    @Param("referralId") referralId: UUID,
  ): List<ReferralUserAssignment>

  @Query(
    """
    SELECT a
    FROM ReferralUserAssignment a 
    WHERE a.referral.id = :referralId AND a.user.id = :userId
        AND (a.deletedBy IS NULL or a.deletedAt IS NULL)
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
    WHERE a.referral.id = :referralId
        AND (a.deletedBy IS NULL or a.deletedAt IS NULL)
    """,
  )
  fun findByReferralId(
    @Param("referralId") referralId: UUID,
  ): List<ReferralUserAssignment>

  @Query(
    """
    SELECT a
    FROM ReferralUserAssignment a 
    WHERE a.referral.id = :referralId AND a.user.id = :userId
        AND a.deletedBy IS NOT NULL AND a.deletedAt IS NOT NULL
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
    WHERE a.referral.id = :referralId
        AND a.deletedBy IS NOT NULL AND a.deletedAt IS NOT NULL
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
    WHERE a.referral.id = :referralId AND a.user.id = :userId
    """,
  )
  fun updateByReferralIdAndUserId(
    @Param("referralId") referralId: UUID,
    @Param("userId") userId: UUID,
    @Param("createdBy") createdBy: UUID,
    @Param("createdAt") createdAt: LocalDateTime,
  ): List<ReferralUserAssignment>

  @Modifying
  @Query(
    """
    UPDATE ReferralUserAssignment a
    SET a.deletedBy = :deletedBy, a.deletedAt = :deletedAt
    WHERE a.referral.id = :referralId AND a.user.id = :userId
    """,
  )
  fun markDeletedByReferralIdAndUserId(
    @Param("referralId") referralId: UUID,
    @Param("userId") userId: UUID,
    @Param("deletedBy") deletedBy: UUID,
    @Param("deletedAt") deletedAt: LocalDateTime,
  ): List<ReferralUserAssignment>
}
