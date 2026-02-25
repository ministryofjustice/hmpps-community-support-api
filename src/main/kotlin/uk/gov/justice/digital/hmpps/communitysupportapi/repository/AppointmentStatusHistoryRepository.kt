package uk.gov.justice.digital.hmpps.communitysupportapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AppointmentStatusHistory
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AppointmentStatusHistoryId
import java.util.UUID

interface AppointmentStatusHistoryRepository : JpaRepository<AppointmentStatusHistory, AppointmentStatusHistoryId> {
  fun findAllByAppointmentIdIn(appointmentIds: List<UUID>): List<AppointmentStatusHistory>
}
