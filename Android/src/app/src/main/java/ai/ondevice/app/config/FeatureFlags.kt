package ai.ondevice.app.config

/**
 * Feature flags for gradual rollout and emergency killswitch.
 *
 * In production, these should be controlled by Firebase Remote Config
 * or similar remote configuration service.
 */
object FeatureFlags {

    /**
     * Enable self-hosted Gemma models from ondevice-ai HuggingFace org.
     *
     * If false, falls back to OAuth flow with official Google repos.
     * Use as killswitch if legal issues arise.
     */
    var enableSelfHostedGemma: Boolean = true

    /**
     * Percentage of users who see self-hosted flow (0-100).
     * Use for gradual rollout:
     * - 10 = 10% of users get self-hosted, 90% get OAuth
     * - 100 = all users get self-hosted
     */
    var selfHostedRolloutPercentage: Int = 100

    /**
     * Enable strict checksum verification.
     * If false, downloads proceed even if checksum fails (logs warning only).
     */
    var strictChecksumVerification: Boolean = true

    /**
     * Enable analytics logging for Gemma downloads.
     */
    var enableGemmaAnalytics: Boolean = true

    /**
     * Emergency disable for Gemma downloads entirely.
     * Use if major security issue discovered.
     */
    var enableGemmaDownloads: Boolean = true

    /**
     * Check if user is in rollout cohort for self-hosted Gemma.
     */
    fun isUserInSelfHostedCohort(userId: String?): Boolean {
        if (!enableSelfHostedGemma) return false
        if (selfHostedRolloutPercentage == 100) return true
        if (selfHostedRolloutPercentage == 0) return false

        // Use stable hash of user ID to assign cohort
        val hash = (userId ?: "default").hashCode()
        val bucket = Math.abs(hash % 100)
        return bucket < selfHostedRolloutPercentage
    }
}
