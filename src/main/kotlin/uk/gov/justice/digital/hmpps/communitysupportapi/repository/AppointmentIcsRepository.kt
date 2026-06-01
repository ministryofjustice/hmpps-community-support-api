package uk.gov.justice.digital.hmpps.communitysupportapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AppointmentIcs
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AppointmentType
import java.util.UUID

interface AppointmentIcsRepository : JpaRepository<AppointmentIcs, UUID> {
  fun findByAppointmentReferralId(referralId: UUID): List<AppointmentIcs>
  fun findAllByAppointmentIdIn(appointmentIds: List<UUID>): List<AppointmentIcs>

  fun findTopByAppointmentIdOrderByCreatedAtDesc(appointmentId: UUID): AppointmentIcs?
  fun findTopByAppointmentIdAndAppointmentTypeOrderByCreatedAtDesc(
    appointmentId: UUID,
    appointmentType: AppointmentType,
  ): AppointmentIcs?

  @Query(
"""
          SELECT a FROM AppointmentIcs a
              WHERE a.appointment.referral.id = :referralId
                AND a.appointment.type = :appointmentType
              ORDER BY a.createdAt DESC
              LIMIT 1
      """,
  )
  fun findLatestIcsByReferralId(
    referralId: UUID,
    @Param("appointmentType") appointmentType: AppointmentType,
  ): AppointmentIcs?
}
