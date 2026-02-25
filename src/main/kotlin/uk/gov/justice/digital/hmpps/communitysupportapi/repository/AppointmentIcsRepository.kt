package uk.gov.justice.digital.hmpps.communitysupportapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AppointmentIcs
import java.util.UUID

interface AppointmentIcsRepository : JpaRepository<AppointmentIcs, UUID>
