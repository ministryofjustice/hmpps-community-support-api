package uk.gov.justice.digital.hmpps.communitysupportapi.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.data.annotation.CreatedDate
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "person")
class Person(
  @Id
  @Column(name = "id")
  val id: UUID,

  @Column(name = "first_name")
  val firstName: String? = null,

  @Column(name = "last_name")
  val lastName: String? = null,

  @Column(name = "sex")
  val sex: String? = null,

  @Column(name = "date_of_birth")
  val dateOfBirth: LocalDate? = null,

  @Column(name = "ethnicity")
  val ethnicity: String? = null,

  @Column(name = "preferred_language")
  val preferredLanguage: String? = null,

  @Column(name = "disability")
  val disability: String? = null,

  @Column(name = "neurodiverse_conditions")
  val neurodiverseConditions: String? = null,

  @Column(name = "religion_or_belief")
  val religionOrBelief: String? = null,

  @Column(name = "gender_identity")
  val genderIdentity: String? = null,

  @Column(name = "transgender")
  val transgender: Boolean? = null,

  @Column(name = "sexual_orientation")
  val sexualOrientation: String? = null,

  @Column(name = "created_at", nullable = false)
  @CreatedDate
  val createdAt: LocalDateTime,
)
