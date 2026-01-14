package uk.gov.justice.digital.hmpps.communitysupportapi.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "person_additional_details")
class PersonAdditionalDetails(
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  val id: UUID? = null,

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "person_id", nullable = false)
  val person: Person,

  @Column(name = "ethnicity")
  val ethnicity: String? = null,

  @Column(name = "preferred_language")
  val preferredLanguage: String? = null,

  @Column(name = "neurodiverse_conditions")
  val neurodiverseConditions: String? = null,

  @Column(name = "religion_or_belief")
  val religionOrBelief: String? = null,

  @Column(name = "transgender")
  val transgender: String? = null,

  @Column(name = "sexual_orientation")
  val sexualOrientation: String? = null,

  @Column(name = "address")
  val address: String? = null,

  @Column(name = "phone_number")
  val phoneNumber: String? = null,

  @Column(name = "email_address")
  val emailAddress: String? = null,
)
