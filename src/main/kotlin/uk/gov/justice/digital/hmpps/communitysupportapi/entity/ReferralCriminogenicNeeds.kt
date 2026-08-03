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
@Table(name = "referral_criminogenic_needs")
class ReferralCriminogenicNeeds(
  @Id
  val id: UUID,

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "referral_id", nullable = false)
  val referral: Referral,

  @Column(name = "has_accommodation_needs")
  val hasAccommodationNeeds: Boolean? = null,

  @Column(name = "accommodation_details")
  val accommodationDetails: String? = null,

  @Column(name = "has_employment_education_needs")
  val hasEmploymentEducationNeeds: Boolean? = null,

  @Column(name = "employment_education_details")
  val employmentEducationDetails: String? = null,

  @Column(name = "has_financial_needs")
  val hasFinancialNeeds: Boolean? = null,

  @Column(name = "financial_details")
  val financialDetails: String? = null,

  @Column(name = "has_personal_relationships_community_needs")
  val hasPersonalRelationshipsCommunityNeeds: Boolean? = null,

  @Column(name = "personal_relationships_community_details")
  val personalRelationshipsCommunityDetails: String? = null,

  @Column(name = "has_drug_use_needs")
  val hasDrugUseNeeds: Boolean? = null,

  @Column(name = "drug_use_details")
  val drugUseDetails: String? = null,

  @Column(name = "has_alcohol_use_needs")
  val hasAlcoholUseNeeds: Boolean? = null,

  @Column(name = "alcohol_use_details")
  val alcoholUseDetails: String? = null,

  @Column(name = "has_health_wellbeing_needs")
  val hasHealthWellbeingNeeds: Boolean? = null,

  @Column(name = "health_wellbeing_details")
  val healthWellbeingDetails: String? = null,

  @Column(name = "has_thinking_behaviours_attitude_needs")
  val hasThinkingBehavioursAttitudeNeeds: Boolean? = null,

  @Column(name = "thinking_behaviours_attitude_details")
  val thinkingBehavioursAttitudeDetails: String? = null,

  @Column(name = "updated_at", nullable = false)
  val updatedAt: OffsetDateTime,

  @Column(name = "updated_by", nullable = false)
  val updatedBy: UUID,
)
