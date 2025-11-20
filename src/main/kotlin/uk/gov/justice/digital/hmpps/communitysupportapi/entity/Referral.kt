package uk.gov.justice.digital.hmpps.communitysupportapi.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "referral")
class Referral(
  @Id
  val id: UUID,

  @Column(name = "first_name")
  val firstName: String? = null,

  @Column(name = "last_name")
  val lastName: String? = null,

  @Column(nullable = false)
  val crn: String,

  @Column(name = "reference_number", nullable = false)
  val referenceNumber: String,

  val sex: String? = null,

  @Column(name = "date_of_birth")
  val dateOfBirth: LocalDate? = null,

  val ethnicity: String? = null,

  @Column(name = "intervention_name")
  val interventionName: String? = null,

  @Column(name = "created_at", nullable = false)
  val createdAt: LocalDateTime = LocalDateTime.now(),
)
