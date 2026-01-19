package uk.gov.justice.digital.hmpps.communitysupportapi.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.springframework.data.annotation.CreatedDate
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "referral")
class Referral(
  @Id
  val id: UUID,

  @Column(name = "community_service_provider_id", nullable = false)
  val communityServiceProviderId: UUID,

  @Column(name = "person_id", nullable = false)
  val personId: UUID,

  @Column(name = "crn")
  val crn: String,

  @Column(name = "reference_number")
  var referenceNumber: String? = null,

  @Column(name = "created_at", nullable = false)
  @CreatedDate
  val createdAt: LocalDateTime,

  @Column(name = "updated_at")
  val updatedAt: LocalDateTime? = null,

  @Column(name = "urgency")
  val urgency: Boolean? = null,

  @OneToMany(mappedBy = "referral", cascade = [CascadeType.PERSIST, CascadeType.MERGE], orphanRemoval = true)
  val referralEvents: MutableList<ReferralEvent> = mutableListOf(),
) {
  val submittedEvent: ReferralEvent?
    get() = referralEvents.firstOrNull { it.eventType == "SUBMITTED" }

  fun addEvent(event: ReferralEvent) {
    referralEvents.add(event)
    event.referral = this
  }
}
