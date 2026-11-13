package uk.gov.justice.digital.hmpps.communitysupportapi.factory

import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActorType
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Referral
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralEvent
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralEventType
import java.time.OffsetDateTime
import java.util.UUID

class ReferralEventFactory(private val referral: Referral) {
  fun appointmentFeedbackSent(actorId: UUID): ReferralEvent = ReferralEvent(
    id = UUID.randomUUID(),
    referral = this.referral,
    eventType = ReferralEventType.APPOINTMENT_FEEDBACK_SENT,
    createdAt = OffsetDateTime.now(),
    actorType = ActorType.EXTERNAL,
    actorId = actorId,
  )
}
