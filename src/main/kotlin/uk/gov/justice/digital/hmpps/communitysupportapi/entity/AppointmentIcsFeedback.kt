package uk.gov.justice.digital.hmpps.communitysupportapi.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "appointment_ics_feedback")
class AppointmentIcsFeedback(

  @Id
  val id: UUID = UUID.randomUUID(),

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "appointment_ics_id", nullable = false)
  val appointmentIcs: AppointmentIcs,

  @Column(name = "record_session_did_session_happen", nullable = false)
  val recordSessionDidSessionHappen: Boolean,

  @Column(name = "record_session_how_session_took_place")
  val recordSessionHowSessionTookPlace: String? = null,

  @Column(name = "record_session_not_in_person_reason", columnDefinition = "TEXT")
  val recordSessionNotInPersonReason: String? = null,

  // PDU – only populated when how session took place is IN_PERSON_PROBATION_OFFICE
  @Column(name = "record_session_pdu")
  val recordSessionPdu: String? = null,

  // Address fields – only populated when how session took place is IN_PERSON_OTHER_LOCATION
  @Column(name = "record_session_address_line1")
  val recordSessionAddressLine1: String? = null,

  @Column(name = "record_session_address_line2")
  val recordSessionAddressLine2: String? = null,

  @Column(name = "record_session_town_or_city")
  val recordSessionTownOrCity: String? = null,

  @Column(name = "record_session_county")
  val recordSessionCounty: String? = null,

  @Column(name = "record_session_postcode")
  val recordSessionPostcode: String? = null,

  @Column(name = "session_details_was_person_late")
  val sessionDetailsWasPersonLate: Boolean? = null,

  @Column(name = "session_details_late_reason", columnDefinition = "TEXT")
  val sessionDetailsLateReason: String? = null,

  @Column(name = "session_details_duration")
  val sessionDetailsDuration: String? = null,

  @Column(name = "session_feedback_what_happened", columnDefinition = "TEXT")
  val sessionFeedbackWhatHappened: String? = null,

  @Column(name = "session_feedback_behaviour", columnDefinition = "TEXT")
  val sessionFeedbackBehaviour: String? = null,

  @Column(name = "session_feedback_strengths_identified", columnDefinition = "TEXT")
  val sessionFeedbackStrengthsIdentified: String? = null,

  @Column(name = "issues_concerns_identified", columnDefinition = "TEXT")
  val issuesConcernsIdentified: String? = null,

  @Column(name = "issues_concerns_notify_probation_practitioner")
  val issuesConcernsNotifyProbationPractitioner: Boolean? = null,

  @Column(name = "next_steps_planned_for_next_session", columnDefinition = "TEXT")
  val nextStepsPlannedForNextSession: String? = null,

  @Column(name = "next_steps_actions_before_next_session", columnDefinition = "TEXT")
  val nextStepsActionsBeforeNextSession: String? = null,

  @Column(name = "created_at", nullable = false)
  val createdAt: LocalDateTime = LocalDateTime.now(),

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "created_by")
  val createdBy: ReferralUser? = null,
)
