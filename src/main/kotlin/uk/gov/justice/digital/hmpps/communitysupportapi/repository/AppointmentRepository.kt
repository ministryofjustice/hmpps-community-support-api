package uk.gov.justice.digital.hmpps.communitysupportapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Appointment
import java.util.UUID

interface AppointmentRepository : JpaRepository<Appointment, UUID>
