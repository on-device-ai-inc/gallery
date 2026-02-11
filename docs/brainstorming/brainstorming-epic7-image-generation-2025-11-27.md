# Brainstorming Session Results - Epic 7: AI Image Generation

**Session Date:** 2025-11-27
**Facilitator:** Creative Ideation Specialist (Claude)
**Participant:** Gora

## Session Start

**Focus Area:** Epic 7 - AI Image Generation Retry Strategy
**Input Document:** `/mnt/user-data/outputs/stable-diffusion-implementation-attempt.md`
**Approach:** To be selected

## Executive Summary

**Topic:** Determining the correct approach for implementing AI image generation in OnDevice AI Android app after a failed Stable Diffusion implementation attempt.

**Session Goals:**
- Analyze why the previous implementation failed (MediaPipe + Stable Diffusion)
- Identify the correct inference framework for on-device image generation
- Select appropriate model variant (balancing quality, speed, size)
- Define integration architecture that works with existing codebase
- Create actionable path forward with validation checkpoints

**Context from Post-Mortem:**

| Aspect | Details |
|--------|---------|
| **Previous Model** | Stable Diffusion 1.5 |
| **Previous Framework** | MediaPipe (attempted) |
| **Development System** | NVIDIA DGX Spark |
| **Target Platform** | Android (ARM64, 4-8GB RAM devices) |

**Key Failures Documented:**
1. Kotlin compilation errors during Android build
2. MediaPipe model conversion issues on DGX Spark
3. Integration architecture problems

**Root Cause Hypotheses:**
| Hypothesis | Confidence | Implication |
|------------|------------|-------------|
| MediaPipe doesn't support generative/diffusion models | 70% | Wrong framework choice |
| Model too complex for mobile | 50% | Need lighter variant |
| JNI/Native code compilation issues | 60% | Need cleaner dependencies |
| Wrong conversion path | 40% | Research correct pipeline |

**Techniques Used:** First Principles Thinking, Five Whys, Assumption Reversal, What If Scenarios

**Total Ideas Generated:** In progress...

---

## Technique Sessions

### Technique 1: First Principles Thinking

**Goal:** Strip away assumptions to find fundamental truths

#### First Principle Established: "EcoCash Principle"

> We're not building "poor person's AI" - we're building ACTUAL professional-quality AI that happens to work offline. Quality parity is non-negotiable.

**Non-Negotiable Requirements:**
- 512x512 minimum output (preferably 768x768)
- Photorealistic AND artistic styles
- Quality indistinguishable from cloud services at mobile screen sizes
- Text-to-image primary use case

**Acceptable Trade-offs:**
- Generation time: 2-5 minutes (users will wait for FREE + OFFLINE)
- Model size: 2-4GB (one-time download)
- Device requirements: 6-8GB RAM (mid-range phones)
- Limited styles: 1-2 models vs dozens

#### Critical Discovery: Codebase Analysis

**The infrastructure already exists!** Analysis revealed:

| Component | Status | Details |
|-----------|--------|---------|
| Model Definition | ✅ Complete | `MODEL_IMAGE_GENERATION_STABLE_DIFFUSION` - 1.9GB SD 1.5 |
| MediaPipe Dependency | ✅ Included | `mediapipe-tasks-imagegen` v0.10.21 |
| Image Display UI | ✅ Complete | `ChatMessageImageWithHistory`, progress indicators |
| Configuration | ✅ Complete | Iteration slider (5-50 steps) |
| Task Registration | ❌ Missing | No `IMAGE_GENERATION` task ID |
| Task Class | ❌ Missing | No `ImageGenerationTask` |
| ViewModel | ❌ Missing | No inference wrapper |
| Screen UI | ❌ Missing | No prompt input screen |

**Key Insight:** Post-mortem hypothesis DISPROVEN. MediaPipe DOES support image generation (v0.10.21 `tasks-vision-image-generator`). The implementation was simply never completed.

**Readiness Assessment:** 60% complete - this is a "finish the implementation" problem, not a "can we do it?" problem.

---

## Idea Categorization

### Immediate Opportunities
_Ideas ready to implement now_

{{immediate_opportunities}}

### Future Innovations
_Ideas requiring development/research_

{{future_innovations}}

### Moonshots
_Ambitious, transformative concepts_

{{moonshots}}

---

## Key Themes Identified

{{key_themes}}

---

## Insights and Learnings

{{insights_learnings}}

---

## Action Planning

### Top 3 Priority Ideas

#### #1 Priority: {{priority_1_name}}

- **Rationale:** {{priority_1_rationale}}
- **Next Steps:** {{priority_1_steps}}
- **Resources Needed:** {{priority_1_resources}}

#### #2 Priority: {{priority_2_name}}

- **Rationale:** {{priority_2_rationale}}
- **Next Steps:** {{priority_2_steps}}
- **Resources Needed:** {{priority_2_resources}}

#### #3 Priority: {{priority_3_name}}

- **Rationale:** {{priority_3_rationale}}
- **Next Steps:** {{priority_3_steps}}
- **Resources Needed:** {{priority_3_resources}}

---

## Reflection and Follow-up

### What Worked Well
{{what_worked}}

### Areas for Further Exploration
{{areas_exploration}}

### Recommended Follow-up Techniques
{{recommended_techniques}}

### Questions That Emerged
{{questions_emerged}}

### Next Session Planning
- **Suggested Topics:** {{followup_topics}}
- **Preparation Needed:** {{preparation}}

---

_Session facilitated using the BMAD CIS brainstorming framework_
