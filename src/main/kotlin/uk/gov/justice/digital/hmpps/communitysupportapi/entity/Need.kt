package uk.gov.justice.digital.hmpps.communitysupportapi.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

/**
 * Needs are a set of static data, see V103__seed_need_reference_data.sql for information
 *
 * A need, e.g. 'Accommodation' or 'Drug use' represent types or areas of support that
 * a Service User might need.
 */
@Entity
@Table(name = "need")
data class Need(
  @Id
  val id: UUID,

  @Column(name = "label", nullable = false)
  val label: String,

  @Column(name = "order_number", nullable = false)
  val orderNumber: Int,
)
