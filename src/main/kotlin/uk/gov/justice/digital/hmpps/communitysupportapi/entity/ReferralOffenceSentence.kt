package uk.gov.justice.digital.hmpps.communitysupportapi.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "referral_offence_sentence")
class ReferralOffenceSentence(
  @Id
  val id: UUID,

  @Column(name = "referral_id", nullable = false)
  val referralId: UUID,

  @Column(name = "person_id", nullable = false)
  val personId: UUID,

  @Column(name = "offence")
  var offence: String? = null,

  @Column(name = "offence_sub_category")
  var offenceSubCategory: String? = null,

  @Column(name = "outcome")
  var outcome: String? = null,

  @Column(name = "sentence_end_date")
  var sentenceEndDate: LocalDate? = null,

  @Column(name = "expected_release_date")
  var expectedReleaseDate: LocalDate? = null,

  @Column(name = "has_licence_conditions_or_exclusion_zones")
  var hasLicenceConditionsOrZones: Boolean? = null,

  @Column(name = "licence_conditions_or_exclusion_zones_details")
  var licenceConditionsOrZonesDetails: String? = null,

  @Column(name = "created_at", nullable = false)
  val createdAt: OffsetDateTime,

  @Column(name = "created_by", nullable = false)
  val createdBy: UUID,

  @Column(name = "updated_at")
  var updatedAt: OffsetDateTime? = null,

  @Column(name = "updated_by")
  var updatedBy: UUID? = null,
)
