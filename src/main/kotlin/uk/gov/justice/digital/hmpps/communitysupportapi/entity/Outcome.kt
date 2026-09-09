package uk.gov.justice.digital.hmpps.communitysupportapi.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

enum class OutcomeSetting {
  CUSTODY,
  COMMUNITY,
  ALL,
}

@Entity
@Table(name = "outcome")
class Outcome(
  @Id
  @Column(name = "id")
  val id: UUID,

  @Column(name = "need_id", nullable = false)
  val needId: UUID,

  @Column(name = "text", nullable = false)
  val text: String,

  @Column(name = "order_number", nullable = false)
  val orderNumber: Int,

  @Column(name = "setting", nullable = false)
  @Enumerated(EnumType.STRING)
  val setting: OutcomeSetting,
)
