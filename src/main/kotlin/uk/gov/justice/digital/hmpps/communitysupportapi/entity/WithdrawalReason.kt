package uk.gov.justice.digital.hmpps.communitysupportapi.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "withdrawal_reason")
data class WithdrawalReason(
  @Id
  val id: UUID,

  @Column(name = "name", nullable = false)
  val name: String,

  @Column(name = "group_name", nullable = false)
  val group: String,
)
