# OnDevice AI — Project Conventions

## Project Overview

**Name:** OnDevice AI
**Purpose:** On-device AI inference Android application — 100% local, privacy-first
**Tech Stack:** Kotlin, Jetpack Compose, Material 3, Room Database, LiteRT-LM, Dagger Hilt
**Repository:** https://github.com/on-device-ai-inc/on-device-ai
**Primary Branch:** development → main

## OpenSpec Workflow

1. `/openspec-proposal <feature>` — Create spec before any code
2. Review and approve
3. `/openspec-apply <feature>` — Implement with TDD + CI + Visual loops
4. `/openspec-archive <feature>` — Archive when complete

## Key Paths

- Source: `Android/src/app/src/main/java/ai/ondevice/app/`
- Specs: `OnDeviceAI-OpenSpec/`
- Changes: `openspec/changes/`
- Archive: `openspec/archive/`

## Device

- Package: `ai.ondevice.app`
- Test device: Samsung S22 Ultra (R3CT10HETMM)
- CI: GitHub Actions (`on-device-ai-inc/on-device-ai`)
