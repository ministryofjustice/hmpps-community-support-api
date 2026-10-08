package uk.gov.justice.digital.hmpps.communitysupportapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AppointmentHistory
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AppointmentType
import java.util.UUID

interface AppointmentIcsRepository : JpaRepository<AppointmentHistory, UUID> {
  fun findByAppointmentReferralId(referralId: UUID): List<AppointmentHistory>
  fun findByAppointmentReferralIdOrderByCreatedAtDesc(referralId: UUID): List<AppointmentHistory>
  fun findAllByAppointmentIdIn(appointmentIds: List<UUID>): List<AppointmentHistory>
  fun findAllByAppointmentIdInOrderByCreatedAtDesc(appointmentIs: List<UUID>): List<AppointmentHistory>

  fun findTopByAppointmentIdOrderByCreatedAtDesc(appointmentId: UUID): AppointmentHistory?
  fun findTopByAppointmentIdAndAppointmentTypeOrderByCreatedAtDesc(
    appointmentId: UUID,
    appointmentType: AppointmentType,
  ): AppointmentHistory?

  @Query(
"""
          SELECT a FROM AppointmentHistory a
              WHERE a.appointment.referral.id = :referralId
                AND a.appointment.type = :appointmentType
              ORDER BY a.createdAt DESC
              LIMIT 1
      """,
  )
  fun findLatestIcsByReferralId(
    referralId: UUID,
    @Param("appointmentType") appointmentType: AppointmentType,
  ): AppointmentHistory?

  @Query(
    """
      SELECT a FROM AppointmentHistory a
        WHERE a.appointment.referral.id = :referralId
          AND a.appointment.type IN :appointmentTypes
        ORDER BY a.createdAt DESC
    """,
  )
  fun findByReferralIdAndTypesOrderByCreatedAtDesc(
    @Param("referralId") referralId: UUID,
    @Param("appointmentTypes") appointmentTypes: List<AppointmentType>,
  ): List<AppointmentHistory>
}
