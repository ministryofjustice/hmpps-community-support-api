package uk.gov.justice.digital.hmpps.communitysupportapi.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "action_plan_template")
data class ActionPlanTemplate(
  @Id
  @Column(name = "id")
  val id: UUID,

  @Column(name = "active_global")
  val activeGlobal: Boolean = false,
)
