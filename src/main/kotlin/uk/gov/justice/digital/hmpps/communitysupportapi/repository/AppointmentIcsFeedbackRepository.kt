package uk.gov.justice.digital.hmpps.communitysupportapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AppointmentIcsFeedback
import java.util.UUID

interface AppointmentIcsFeedbackRepository : JpaRepository<AppointmentIcsFeedback, UUID>
