package uk.gov.justice.digital.hmpps.communitysupportapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralNote
import java.time.LocalDateTime
import java.util.UUID

interface ReferralNoteOrNonContactSessionRow {
  val id: UUID
  val createdAt: LocalDateTime
  val createdBy: String
  val title: String
  val details: String
}

interface ReferralNotesRepository : JpaRepository<ReferralNote, UUID> {
  fun findAllByReferralIdAndDeletedAtIsNullOrderByCreatedAtDesc(referralID: UUID): List<ReferralNote>

  @Query(
    value = """
      SELECT items.id AS id, items.created_at AS "createdAt", items.created_by AS "createdBy",
        items.title AS title, items.details AS details
      FROM (
        SELECT id, referral_id, created_at, created_by, title, details
        FROM referral_notes WHERE deleted_at IS NULL
        UNION ALL
        SELECT id, referral_id, created_at, created_by, title, details
        FROM referral_non_contact_session WHERE deleted_at IS NULL
      ) items
      WHERE items.referral_id = :referralId
      ORDER BY items.created_at DESC, items.id DESC
      LIMIT :limit OFFSET :offset
      """,
    nativeQuery = true,
  )
  fun findPagedNoteAndNonContactSessionRows(
    @Param("referralId") referralId: UUID,
    @Param("limit") limit: Int,
    @Param("offset") offset: Long,
  ): List<ReferralNoteOrNonContactSessionRow>

  @Query(
    value = """
      SELECT COUNT(*) FROM (
        SELECT referral_id FROM referral_notes WHERE deleted_at IS NULL
        UNION ALL
        SELECT referral_id FROM referral_non_contact_session WHERE deleted_at IS NULL
      ) items
      WHERE items.referral_id = :referralId
      """,
    nativeQuery = true,
  )
  fun countNoteAndNonContactSessionRows(@Param("referralId") referralId: UUID): Long
}
