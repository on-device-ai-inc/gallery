# GitHub Organization Setup Guide
## Complete Instructions for OnDevice AI Inc. / OnDevice AI

**Purpose:** Create corporate GitHub organization and fork Google AI Edge Gallery with clean IP ownership

**Prerequisites:**
- GitHub account (personal account - you'll convert to organization)
- Corporate email address (setup Activity 20 first)
- Certificate of Incorporation (for verification if needed)

**Timeline:** 30-45 minutes

**Cost:** $0 (free tier) or $4/user/month (Team plan for private repos)

---

## STEP 1: Create GitHub Organization

### Option A: Create New Organization (Recommended)

1. **Go to:** https://github.com/settings/organizations

2. **Click:** "New organization"

3. **Choose plan:**
   - **Free:** Public repositories only, unlimited collaborators
   - **Team ($4/user/month):** Private repositories, advanced features
   - **Recommendation:** Start with Free, upgrade when needed

4. **Organization details:**
   - **Organization name:** `gora-ai` or `ondevice-ai` (based on corporate name from NUANS)
     - Must be available (GitHub will show if taken)
     - Cannot be changed easily later - choose carefully!
   - **Contact email:** Use corporate email (e.g., `info@on-device.app`)
   - **Organization type:** Business

5. **Billing:**
   - If Free: No payment needed
   - If Team: Enter corporate credit card or bank account

6. **Click:** "Next" → "Complete setup"

### Option B: Convert Existing Personal Account (If Applicable)

If you already have repos under personal account:

1. **Create new personal account first** (important - don't lose your personal identity!)
2. **Transfer ownership** of personal account to new account
3. **Convert old account to organization:**
   - Settings → Account → "Convert account to organization"
   - Follow prompts

**Warning:** Only do this if you want to convert an existing account. Most users should use Option A.

---

## STEP 2: Configure Organization Profile

1. **Go to:** https://github.com/[YOUR-ORG-NAME]

2. **Click:** "Settings" (organization settings, not personal)

3. **Profile:**
   - **Display name:** OnDevice AI Inc. (or OnDevice AI)
   - **Description:** "On-device AI applications for privacy-focused users"
   - **Email:** info@on-device.app (corporate email)
   - **Website:** https://on-device.app (corporate website when ready)
   - **Location:** Ontario, Canada
   - **Twitter:** @ondeviceai (if applicable)

4. **Avatar/Logo:**
   - Upload company logo (if you have one)
   - Or use GitHub's default until you create a logo
   - Recommended size: 400x400 pixels

5. **Click:** "Update profile"

---

## STEP 3: Set Up Teams and Permissions

1. **Go to:** Organization Settings → Teams

2. **Create teams:**

   **Team 1: Core Development**
   - Name: `core-development`
   - Description: "Core developers with write access"
   - Members: Add yourself
   - Permissions: Write (can push code, create branches)

   **Team 2: Admins (Optional)**
   - Name: `admins`
   - Description: "Organization administrators"
   - Members: Add yourself
   - Permissions: Admin (full control)

3. **Click:** "Create team" for each

---

## STEP 4: Configure Organization Settings

### Security Settings

1. **Go to:** Settings → Security → Code security and analysis

2. **Enable:**
   - ✅ Dependency graph (tracks dependencies)
   - ✅ Dependabot alerts (security vulnerabilities)
   - ✅ Dependabot security updates (automatic PRs for fixes)

3. **Two-factor authentication:**
   - Settings → Security → Two-factor authentication
   - **Require 2FA for all members** (recommended for security)

### Member Privileges

1. **Go to:** Settings → Member privileges

2. **Configure:**
   - **Base permissions:** Read (default for organization members)
   - **Repository creation:** Allow members to create repositories
   - **Repository forking:** Allow forking of private repositories (if using Team plan)
   - **Pages creation:** Allow members to create GitHub Pages

---

## STEP 5: Fork Google AI Edge Gallery Repository

**⚠️ CRITICAL:** This must be done as the ORGANIZATION, not your personal account!

### Method 1: Web Interface (Easiest)

1. **Make sure you're logged into GitHub**

2. **Go to:** https://github.com/google-ai-edge/ai-edge-gallery

3. **Click:** "Fork" button (top right)

4. **IMPORTANT:** In the "Owner" dropdown:
   - **DO NOT select your personal account**
   - **SELECT:** Your organization name (e.g., `gora-ai`)

5. **Repository name:**
   - Keep as: `ai-edge-gallery`
   - Or rename to: `ondevice-ai` (if you prefer)

6. **Description:** "On-device AI mobile app - forked from Google AI Edge Gallery"

7. **Settings:**
   - ✅ **Copy the main branch only** (uncheck if you want all branches)
   - **Recommendation:** Copy all branches (development history may be useful)

8. **Click:** "Create fork"

9. **Wait:** 30-60 seconds for GitHub to copy repository

10. **Result:** You now have `https://github.com/gora-ai/ai-edge-gallery`

### Method 2: Command Line (Alternative)

If you prefer command line:

```bash
# 1. Create new repo in organization first (via GitHub web UI)
# Organization Settings → Repositories → New repository
# Name: ai-edge-gallery or ondevice-ai
# Visibility: Public
# DO NOT initialize with README (you're importing)

# 2. Clone Google's repo
git clone https://github.com/google-ai-edge/ai-edge-gallery.git
cd ai-edge-gallery

# 3. Change remote to your organization
git remote set-url origin https://github.com/gora-ai/ai-edge-gallery.git

# 4. Push to your organization
git push -u origin main
git push --all  # Push all branches
git push --tags # Push all tags

# 5. Verify
git remote -v
# Should show: origin https://github.com/gora-ai/ai-edge-gallery.git
```

---

## STEP 6: Configure Forked Repository

1. **Go to:** https://github.com/gora-ai/ai-edge-gallery

2. **Click:** "Settings" (repository settings)

3. **General:**
   - **Description:** "On-device AI mobile app for privacy-focused users"
   - **Website:** https://on-device.app (when ready)
   - **Topics:** Add tags: `android`, `ai`, `on-device`, `privacy`, `kotlin`, `gemma`
   - ✅ **Allow forking** (if you want others to fork your fork)
   - ✅ **Issues** (enable issue tracking)
   - ✅ **Discussions** (enable if you want community feedback)

4. **Branches:**
   - **Default branch:** Verify it's `main` (or create a `develop` branch if you want)
   - **Branch protection rules (Recommended):**
     - Click "Add rule"
     - Branch name pattern: `main`
     - ✅ Require pull request reviews before merging
     - ✅ Require status checks to pass before merging
     - ✅ Require branches to be up to date before merging
     - Click "Create"

5. **Collaborators and teams:**
   - Add teams with appropriate permissions:
     - `core-development` → Write access
     - `admins` → Admin access

---

## STEP 7: Set Up Local Development

Now clone YOUR fork (not Google's original) to your development machine:

```bash
# 1. Navigate to your development directory
cd ~/Development  # or wherever you keep projects

# 2. Clone YOUR organization's fork
git clone https://github.com/gora-ai/ai-edge-gallery.git

# 3. Enter the directory
cd ai-edge-gallery

# 4. Add Google's repo as upstream (to pull future updates)
git remote add upstream https://github.com/google-ai-edge/ai-edge-gallery.git

# 5. Verify remotes
git remote -v
# Should show:
# origin    https://github.com/gora-ai/ai-edge-gallery.git (fetch)
# origin    https://github.com/gora-ai/ai-edge-gallery.git (push)
# upstream  https://github.com/google-ai-edge/ai-edge-gallery.git (fetch)
# upstream  https://github.com/google-ai-edge/ai-edge-gallery.git (push)

# 6. Create development branch
git checkout -b development
git push -u origin development

# 7. Set up Android project
cd Android
# Follow existing setup instructions in Android/README.md
```

---

## STEP 8: Update Repository Metadata

Update key files to reflect corporate ownership:

### 8.1 Update README.md

```bash
# Open README.md
nano README.md

# Add at top:
# OnDevice AI
# Forked from [Google AI Edge Gallery](https://github.com/google-ai-edge/ai-edge-gallery)
# Copyright 2026 OnDevice AI Inc.
# Original Copyright 2025 Google LLC

# Update description with your value proposition
# Add corporate contact info
# Add link to your website when ready

# Commit changes
git add README.md
git commit -m "Update README with corporate branding

🤖 Generated with [Claude Code](https://claude.com/claude-code)

Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>"

git push origin development
```

### 8.2 Verify LICENSE

```bash
# Check current license
cat LICENSE

# Should be Apache License 2.0 (from Google)
# This is good - keep it!
# Apache 2.0 allows commercial use, modification, distribution

# You can add a NOTICE file for corporate attribution:
cat > NOTICE << 'EOF'
OnDevice AI
Copyright 2026 OnDevice AI Inc.

This product includes software developed by Google LLC (google-ai-edge/ai-edge-gallery)
Licensed under the Apache License, Version 2.0

For the original work, see: https://github.com/google-ai-edge/ai-edge-gallery
EOF

git add NOTICE
git commit -m "Add corporate NOTICE file

🤖 Generated with [Claude Code](https://claude.com/claude-code)

Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>"

git push origin development
```

---

## STEP 9: Configure GitHub Actions (CI/CD)

If the original repo has GitHub Actions:

1. **Go to:** Repository → Actions

2. **Review existing workflows:**
   - Check if Google has CI/CD configured
   - Workflows are in `.github/workflows/`

3. **Enable Actions:**
   - Click "I understand my workflows, go ahead and enable them"

4. **Configure secrets (if needed):**
   - Settings → Secrets and variables → Actions
   - Add any API keys, signing keys, etc.
   - **Never commit secrets to code!**

---

## STEP 10: Link to Google Play Developer Account

When you create Google Play account (Activity 17):

1. **Update app build.gradle:**
   ```kotlin
   // Update package name to corporate namespace
   namespace = "ai.ondevice.app"  // or ai.ondevice.app

   // Update app name
   android {
       defaultConfig {
           applicationId = "ai.ondevice.app"
           versionName = "2.0.0"  // New major version for corporate release
       }
   }
   ```

2. **Update strings.xml:**
   ```xml
   <resources>
       <string name="app_name">OnDevice AI</string>
       <string name="developer">OnDevice AI Inc.</string>
   </resources>
   ```

3. **Create signing configuration:**
   - Generate new keystore (corporate-owned, not personal)
   - Store keystore securely
   - Add to GitHub Secrets for automated builds

---

## STEP 11: Set Up Project Board (Optional but Recommended)

Track development progress:

1. **Go to:** Repository → Projects → New project

2. **Template:** Board

3. **Name:** "OnDevice AI Development"

4. **Columns:**
   - Backlog
   - To Do
   - In Progress
   - In Review
   - Done

5. **Add issues/tasks:**
   - Rename from "AI Edge Gallery" to "OnDevice AI"
   - Update branding and logos
   - Add persona system (already implemented!)
   - Add token monitoring (already implemented!)
   - Implement web search improvements
   - App store submission
   - etc.

---

## STEP 12: Sync with Upstream (Pull Google's Updates)

Periodically pull updates from Google's original repo:

```bash
# Fetch updates from Google
git fetch upstream

# Switch to main branch
git checkout main

# Merge Google's updates
git merge upstream/main

# If conflicts, resolve them
# Then push to your fork
git push origin main

# Update development branch
git checkout development
git merge main
git push origin development
```

**Frequency:** Check monthly for updates from Google

---

## VERIFICATION CHECKLIST

After completing all steps, verify:

- [ ] Organization created: `gora-ai` or `ondevice-ai`
- [ ] Organization profile complete (description, email, website)
- [ ] Teams created: `core-development` (and optionally `admins`)
- [ ] Repository forked: `https://github.com/gora-ai/ai-edge-gallery`
- [ ] Repository ownership: Shows OnDevice AI Inc. (not personal account)
- [ ] Repository configured: Description, topics, branch protection
- [ ] Local clone: Working copy on development machine
- [ ] Remotes configured: `origin` = your fork, `upstream` = Google
- [ ] Development branch: Created and pushed
- [ ] README updated: Corporate branding added
- [ ] NOTICE file: Corporate attribution added
- [ ] GitHub Actions: Enabled (if applicable)

---

## CRITICAL REMINDERS

### ✅ DO:
- Fork as ORGANIZATION (not personal account)
- Update README with corporate branding
- Keep Apache 2.0 license (required)
- Add NOTICE file for attribution
- Use corporate email for commits
- Configure Git identity:
  ```bash
  git config user.name "OnDevice AI Inc."
  git config user.email "dev@on-device.app"
  ```

### ❌ DON'T:
- Fork to personal account then transfer (IP ownership complications)
- Remove Google's license or copyright
- Claim original authorship
- Violate Apache 2.0 license terms
- Commit API keys or secrets
- Use personal email for corporate commits

---

## IP OWNERSHIP VERIFICATION

**Why this matters:**

✅ **Correct (Corporate Fork):**
- Repository owner: OnDevice AI Inc. (organization)
- All commits from corporate account
- IP = 100% corporate from Day 1
- No assignment of IP needed
- Clean for investors/acquirers

❌ **Wrong (Personal Fork + Transfer):**
- Original owner: Personal account
- Transferred to corporate later
- Potential IP dispute (did you assign it?)
- Requires Section 85 rollover or assignment agreement
- Messy for investors/acquirers

**Verification:**
```bash
# Check repository ownership
curl -s https://api.github.com/repos/gora-ai/ai-edge-gallery | grep '"owner"' -A 10

# Should show:
# "owner": {
#   "login": "gora-ai",
#   "type": "Organization",
#   ...
# }
```

If it shows your personal username, you did it wrong - delete and recreate as organization fork!

---

## NEXT STEPS

After GitHub setup complete:

1. **Begin development:**
   - Work in `development` branch
   - Commit regularly
   - Push to organization repo

2. **Customize app:**
   - Update branding (logo, colors, name)
   - Add/remove features
   - Improve UI/UX

3. **Prepare for launch:**
   - Create release branch
   - Generate signed APK
   - Submit to Google Play (Activity 17)

4. **Document everything:**
   - Log hours in SR&ED tracking
   - Track innovations
   - Keep technical notes

**All future code = corporate IP automatically!** 🎉

---

## TROUBLESHOOTING

**Issue:** Can't see organization in fork dropdown

**Solution:** Make sure you're logged in and the organization is created. Refresh page.

---

**Issue:** Fork shows personal account as owner

**Solution:** You forked to personal account! Delete the fork and recreate, selecting organization in dropdown.

---

**Issue:** Can't push to organization repo

**Solution:** Make sure you're a member of the organization with write permissions. Check organization settings.

---

**Issue:** Don't have corporate email yet

**Solution:** Complete Activity 20 first (set up email/domain). GitHub allows you to add email later.

---

## ESTIMATED TIME

- Organization creation: 10 minutes
- Profile configuration: 5 minutes
- Team setup: 5 minutes
- Fork repository: 2 minutes
- Repository configuration: 10 minutes
- Local clone and setup: 5 minutes
- Update metadata: 5 minutes
- Verification: 3 minutes

**Total: 45 minutes** (first time)

---

## COST SUMMARY

- GitHub Organization (Free tier): **$0/month**
- GitHub Organization (Team plan): **$4/user/month**
- Private repositories: Included in Team plan
- GitHub Actions minutes: 2,000 minutes/month free, then $0.008/minute
- GitHub Packages storage: 500 MB free, then $0.25/GB

**Recommendation:** Start with Free tier ($0), upgrade to Team ($4/month) when you need private repos.

---

**Guide Created:** January 2, 2026
**Status:** Ready to execute after incorporation
**Dependencies:** Corporate email (Activity 20) - can skip initially and add later
**Next Activity:** Activity 17 (Google Play Developer account)
