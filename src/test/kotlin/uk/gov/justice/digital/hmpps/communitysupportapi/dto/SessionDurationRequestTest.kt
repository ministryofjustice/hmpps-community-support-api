package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SessionDurationRequestTest {

  @Test
  fun `formats 1 hour and 45 minutes correctly`() {
    assertThat(SessionDurationRequest(hours = 1, minutes = 45).toDisplayString())
      .isEqualTo("1 hour and 45 minutes")
  }

  @Test
  fun `formats 2 hours and 30 minutes correctly`() {
    assertThat(SessionDurationRequest(hours = 2, minutes = 30).toDisplayString())
      .isEqualTo("2 hours and 30 minutes")
  }

  @Test
  fun `formats whole hours with no minutes`() {
    assertThat(SessionDurationRequest(hours = 1, minutes = 0).toDisplayString())
      .isEqualTo("1 hour")
    assertThat(SessionDurationRequest(hours = 2, minutes = null).toDisplayString())
      .isEqualTo("2 hours")
  }

  @Test
  fun `formats minutes only when hours is 0`() {
    assertThat(SessionDurationRequest(hours = 0, minutes = 30).toDisplayString())
      .isEqualTo("30 minutes")
  }

  @Test
  fun `formats 1 minute correctly (singular)`() {
    assertThat(SessionDurationRequest(hours = 0, minutes = 1).toDisplayString())
      .isEqualTo("1 minute")
    assertThat(SessionDurationRequest(hours = 1, minutes = 1).toDisplayString())
      .isEqualTo("1 hour and 1 minute")
  }

  @Test
  fun `formats zero hours and zero minutes as 0 minutes`() {
    assertThat(SessionDurationRequest(hours = 0, minutes = 0).toDisplayString())
      .isEqualTo("0 minutes")
  }
}
