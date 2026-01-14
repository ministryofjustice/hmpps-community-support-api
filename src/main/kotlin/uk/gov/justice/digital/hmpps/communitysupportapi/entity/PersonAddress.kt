package uk.gov.justice.digital.hmpps.communitysupportapi.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "person_address")
class PersonAddress(
  @Id
  @Column(name = "id", nullable = false)
  val id: UUID? = null,

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "person_id", nullable = false)
  val person: Person? = null,

  @Column(name = "address")
  val address: String? = null,

  @Column(name = "phone_number")
  val phoneNumber: String? = null,

  @Column(name = "email_address")
  val emailAddress: String? = null,

  @Column(name = "created_at", nullable = false)
  val createdAt: LocalDateTime? = null,
)
