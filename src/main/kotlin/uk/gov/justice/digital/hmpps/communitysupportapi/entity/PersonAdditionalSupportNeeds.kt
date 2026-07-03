package uk.gov.justice.digital.hmpps.communitysupportapi.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "person_additional_support_needs")
class PersonAdditionalSupportNeeds(
  @Id
  @Column(name = "id")
  val id: UUID,

  @Column(name = "referral_id", nullable = false)
  val referralId: UUID,

  @Column(name = "person_id", nullable = false)
  val personId: UUID,

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "referral_id", insertable = false, updatable = false)
  val referral: Referral? = null,

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "person_id", insertable = false, updatable = false)
  val person: Person? = null,

  @Column(name = "physical_health_details")
  val physicalHealthDetails: String? = null,

  @Column(name = "mental_emotional_health_details")
  val mentalEmotionalHealthDetails: String? = null,

  @Column(name = "neurodiversity_details")
  val neurodiversityDetails: String? = null,

  @Column(name = "location_travel_details")
  val locationTravelDetails: String? = null,

  @Column(name = "caring_responsibilities_details")
  val caringResponsibilitiesDetails: String? = null,

  @Column(name = "employment_responsibilities_details")
  val employmentResponsibilitiesDetails: String? = null,

  @Column(name = "diversity_details")
  val diversityDetails: String? = null,

  @Column(name = "anything_else_details")
  val anythingElseDetails: String? = null,

  @Column(name = "no_additional_support_needed", nullable = false)
  val noAdditionalSupportNeeded: Boolean = false,

  @Column(name = "interpreter_language")
  val interpreterLanguage: String? = null,

  @Column(name = "created_at")
  val createdAt: OffsetDateTime? = null,

  @Column(name = "updated_at")
  val updatedAt: OffsetDateTime? = null,

  @Column(name = "created_by", nullable = false)
  val createdBy: UUID,

  @Column(name = "updated_by")
  val updatedBy: UUID? = null,
)
