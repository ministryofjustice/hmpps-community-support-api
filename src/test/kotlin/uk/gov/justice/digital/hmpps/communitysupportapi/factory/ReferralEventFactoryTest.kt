package uk.gov.justice.digital.hmpps.communitysupportapi.factory

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActorType
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralEventType
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.ReferralFactory
import java.util.UUID

class ReferralEventFactoryTest {

  @Test
  fun `creates an external appointment feedback event for the referral`() {
    val referral = ReferralFactory().create()
    val actorId = UUID.randomUUID()

    val event = ReferralEventFactory(referral).appointmentFeedbackSent(actorId)

    assertThat(event.referral).isSameAs(referral)
    assertThat(event.eventType).isEqualTo(ReferralEventType.APPOINTMENT_FEEDBACK_SENT)
    assertThat(event.actorType).isEqualTo(ActorType.EXTERNAL)
    assertThat(event.actorId).isEqualTo(actorId)
    assertThat(event.createdAt).isNotNull()
  }
}
