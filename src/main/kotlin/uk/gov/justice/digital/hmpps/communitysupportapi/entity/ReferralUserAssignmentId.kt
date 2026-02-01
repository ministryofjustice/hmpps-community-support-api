package uk.gov.justice.digital.hmpps.communitysupportapi.entity

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import java.io.Serializable
import java.util.UUID

@Embeddable
data class ReferralUserAssignmentId(
  @Column(name = "referral_id")
  var referralId: UUID,

  @Column(name = "user_id")
  val userId: UUID,
) : Serializable
