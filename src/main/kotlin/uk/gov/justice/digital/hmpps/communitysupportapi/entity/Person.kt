package uk.gov.justice.digital.hmpps.communitysupportapi.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "person")
class Person(
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  val id: UUID? = null,

  @Column(name = "identifier", nullable = false, unique = true)
  val identifier: String,

  @Column(name = "first_name", nullable = false)
  val firstName: String,

  @Column(name = "last_name", nullable = false)
  val lastName: String,

  @Column(name = "date_of_birth", nullable = false)
  val dateOfBirth: LocalDate,

  @Column(name = "gender", nullable = false)
  val gender: String,

  @Column(name = "created_at", nullable = false)
  val createdAt: OffsetDateTime = OffsetDateTime.now(),

  @Column(name = "updated_at", nullable = false)
  val updatedAt: OffsetDateTime = OffsetDateTime.now(),

  @OneToOne(
    cascade = [CascadeType.ALL],
    orphanRemoval = true,
    fetch = FetchType.LAZY,
    mappedBy = "person",
  )
  var additionalDetails: PersonAdditionalDetails? = null,
)
