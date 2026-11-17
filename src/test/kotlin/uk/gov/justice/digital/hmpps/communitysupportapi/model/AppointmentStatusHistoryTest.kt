package uk.gov.justice.digital.hmpps.communitysupportapi.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AppointmentHistory
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AppointmentIcsFeedback

class AppointmentStatusHistoryTest {

  @Test
  fun `returns COMPLETED when the session happened`() {
    assertThat(feedback(didSessionHappen = true, didPersonAttend = false).getAppointmentStatusHistoryType())
      .isEqualTo(AppointmentStatusHistoryType.COMPLETED)
  }

  @Test
  fun `returns DID_NOT_ATTEND when the session did not happen and the person did not attend`() {
    assertThat(feedback(didSessionHappen = false, didPersonAttend = false).getAppointmentStatusHistoryType())
      .isEqualTo(AppointmentStatusHistoryType.DID_NOT_ATTEND)
  }

  @Test
  fun `returns DID_NOT_HAPPEN when the session did not happen and the person attended`() {
    assertThat(feedback(didSessionHappen = false, didPersonAttend = true).getAppointmentStatusHistoryType())
      .isEqualTo(AppointmentStatusHistoryType.DID_NOT_HAPPEN)
  }

  private fun feedback(didSessionHappen: Boolean, didPersonAttend: Boolean) = AppointmentIcsFeedback(
    appointmentIcs = mock<AppointmentHistory>(),
    recordSessionDidSessionHappen = didSessionHappen,
    recordSessionDidPersonAttend = didPersonAttend,
  )
}
