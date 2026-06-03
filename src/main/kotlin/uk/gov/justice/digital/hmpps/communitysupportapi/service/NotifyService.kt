package uk.gov.justice.digital.hmpps.communitysupportapi.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.service.notify.NotificationClient

@Service
class NotifyService(
  @Autowired private val notifyClient: NotificationClient,
) {
  fun sendEmail(templateId: String, emailAddress: String, personalisation: Map<String, Any>) {
    notifyClient.sendEmail(templateId, emailAddress, personalisation, null)
  }
}
