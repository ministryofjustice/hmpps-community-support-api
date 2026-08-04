# IPB-2567 Change Summary

## Current Outcome
This branch now contains a single squashed commit for the Action Plan persistence work:
- 6660e88 Implement action plan persistence schema and runtime updates

## Delivered Changes
1. Database migration
- Created and finalized [src/main/resources/db/migration/V16__create_action_plan_tables.sql](src/main/resources/db/migration/V16__create_action_plan_tables.sql).
- Included template and runtime tables:
  - action_plan_template
  - action_plan_step
  - action_plan_step_question
  - action_plan
  - action_plan_step_question_response
  - action_plan_event
- Added column comments for readability.
- Kept answer_type as text with a check constraint (no database enums).
- Enforced relationship constraints:
  - one action plan per referral via unique referral_id
  - one response per question per plan via unique action_plan_id + action_plan_step_question_id
  - many events per action plan
- Folded runtime-delta migration changes into V16 so there is one migration file.

2. Entity classes
- Added:
  - [src/main/kotlin/uk/gov/justice/digital/hmpps/communitysupportapi/entity/ActionPlanTemplate.kt](src/main/kotlin/uk/gov/justice/digital/hmpps/communitysupportapi/entity/ActionPlanTemplate.kt)
  - [src/main/kotlin/uk/gov/justice/digital/hmpps/communitysupportapi/entity/ActionPlanStep.kt](src/main/kotlin/uk/gov/justice/digital/hmpps/communitysupportapi/entity/ActionPlanStep.kt)
  - [src/main/kotlin/uk/gov/justice/digital/hmpps/communitysupportapi/entity/ActionPlanStepQuestion.kt](src/main/kotlin/uk/gov/justice/digital/hmpps/communitysupportapi/entity/ActionPlanStepQuestion.kt)
  - [src/main/kotlin/uk/gov/justice/digital/hmpps/communitysupportapi/entity/ActionPlan.kt](src/main/kotlin/uk/gov/justice/digital/hmpps/communitysupportapi/entity/ActionPlan.kt)
  - [src/main/kotlin/uk/gov/justice/digital/hmpps/communitysupportapi/entity/ActionPlanStepQuestionResponse.kt](src/main/kotlin/uk/gov/justice/digital/hmpps/communitysupportapi/entity/ActionPlanStepQuestionResponse.kt)
  - [src/main/kotlin/uk/gov/justice/digital/hmpps/communitysupportapi/entity/ActionPlanEvent.kt](src/main/kotlin/uk/gov/justice/digital/hmpps/communitysupportapi/entity/ActionPlanEvent.kt)
- Updated runtime entities to match latest contract:
  - ActionPlan is one-to-one with Referral
  - created_by fields present on event and response entities
  - response column mapped as response (not response_text)

3. Repository interfaces
- Added:
  - [src/main/kotlin/uk/gov/justice/digital/hmpps/communitysupportapi/repository/ActionPlanTemplateRepository.kt](src/main/kotlin/uk/gov/justice/digital/hmpps/communitysupportapi/repository/ActionPlanTemplateRepository.kt)
  - [src/main/kotlin/uk/gov/justice/digital/hmpps/communitysupportapi/repository/ActionPlanStepRepository.kt](src/main/kotlin/uk/gov/justice/digital/hmpps/communitysupportapi/repository/ActionPlanStepRepository.kt)
  - [src/main/kotlin/uk/gov/justice/digital/hmpps/communitysupportapi/repository/ActionPlanStepQuestionRepository.kt](src/main/kotlin/uk/gov/justice/digital/hmpps/communitysupportapi/repository/ActionPlanStepQuestionRepository.kt)
  - [src/main/kotlin/uk/gov/justice/digital/hmpps/communitysupportapi/repository/ActionPlanRepository.kt](src/main/kotlin/uk/gov/justice/digital/hmpps/communitysupportapi/repository/ActionPlanRepository.kt)
  - [src/main/kotlin/uk/gov/justice/digital/hmpps/communitysupportapi/repository/ActionPlanStepQuestionResponseRepository.kt](src/main/kotlin/uk/gov/justice/digital/hmpps/communitysupportapi/repository/ActionPlanStepQuestionResponseRepository.kt)
  - [src/main/kotlin/uk/gov/justice/digital/hmpps/communitysupportapi/repository/ActionPlanEventRepository.kt](src/main/kotlin/uk/gov/justice/digital/hmpps/communitysupportapi/repository/ActionPlanEventRepository.kt)

4. Verification performed
- compileKotlin executed successfully after schema/entity changes.

## Notes on Documentation Files
- Previous interim docs [IPB-2567-imp1.md](IPB-2567-imp1.md) and [IPB-2567-imp2-plan.md](IPB-2567-imp2-plan.md) are currently deleted in the working tree by request.
