package uk.gov.justice.digital.hmpps.communitysupportapi.service

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import uk.gov.service.notify.NotificationClient
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class NotifyServiceTest {

  private val notificationClient = mock<NotificationClient>()

  private lateinit var notifyService: NotifyService

  @BeforeEach
  fun setUp() {
    notifyService = NotifyService(notifyClient = notificationClient)
  }

  @Test
  fun `should send email when give email and templateId`() {
    val templateId = UUID.randomUUID().toString()
    notifyService.sendEmail(templateId, "test@justice.gov.uk", mapOf("name" to "value"))
    verify(notificationClient).sendEmail(
      templateId,
      "test@justice.gov.uk",
      mapOf("name" to "value"),
      null,
    )
  }
}
