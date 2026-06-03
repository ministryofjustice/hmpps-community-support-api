package uk.gov.justice.digital.hmpps.communitysupportapi.client

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.IntegrationTestBase
import uk.gov.service.notify.NotificationClient

class NotifyClientIntegrationTest : IntegrationTestBase() {

  @Autowired
  lateinit var notifyClient: NotificationClient

  private val templateId = "2269a690-61e1-4b04-881f-198beb822465"

  @Test
  fun `should send email`() {
    val result = notifyClient.sendEmail(templateId, "interventions-devs@digital.justice.gov.uk", mapOf("name" to "value"), null)
    assertThat(result.subject).isEqualTo("This is a test of Notify for Community Support")
  }
}
