package ai.ondevice.app.persona

/**
 * Defines the available persona variants for the Edge Gallery AI assistant.
 * Each variant represents a different balance between token usage and guidance detail.
 *
 * Variants are ordered from most comprehensive to most minimal:
 * - MAXIMUM: 620 tokens (~31% of 2K context) - Most comprehensive guidance
 * - COMPREHENSIVE: 340 tokens (~17% of 2K context) - Strong guidance with better efficiency
 * - BALANCED: 230 tokens (~11.5% of 2K context) - Sweet spot for most use cases
 * - STREAMLINED: 170 tokens (~8.5% of 2K context) - Core behaviors only
 * - MINIMAL: 110 tokens (~5.5% of 2K context) - Absolute minimum guidance
 */
enum class PersonaVariant {
    /**
     * Maximum guidance (620 tokens).
     * Best for: Complex development tasks, varied workloads, professional use cases.
     * Provides comprehensive behavioral rules, clearest workflow, and explicit constraints.
     */
    MAXIMUM,

    /**
     * Comprehensive guidance (340 tokens).
     * Best for: General development work, professional assistance, balanced performance.
     * Strong core guidance with clear operating principles and good efficiency.
     */
    COMPREHENSIVE,

    /**
     * Balanced guidance (230 tokens) - DEFAULT.
     * Best for: General use, resource-constrained devices, simple to moderate tasks.
     * Essential best practices only, excellent balance between quality and efficiency.
     */
    BALANCED,

    /**
     * Streamlined guidance (170 tokens).
     * Best for: Simple tasks, very resource-constrained devices, casual use.
     * Core behaviors present with minimal structure.
     */
    STREAMLINED,

    /**
     * Minimal guidance (110 tokens).
     * Best for: Ultra-lightweight deployment, very simple tasks, maximum context for conversation.
     * Absolute minimum constraints, relies heavily on base model capabilities.
     */
    MINIMAL;

    /**
     * Returns the approximate token count for this persona variant.
     */
    fun getTokenCount(): Int = when (this) {
        MAXIMUM -> 620
        COMPREHENSIVE -> 340
        BALANCED -> 230
        STREAMLINED -> 170
        MINIMAL -> 110
    }

    /**
     * Returns the percentage of a 2K context window this persona uses.
     */
    fun getContextUsagePercent2K(): Double = when (this) {
        MAXIMUM -> 31.0
        COMPREHENSIVE -> 17.0
        BALANCED -> 11.5
        STREAMLINED -> 8.5
        MINIMAL -> 5.5
    }

    /**
     * Returns a human-readable description of this variant.
     */
    fun getDescription(): String = when (this) {
        MAXIMUM -> "Most comprehensive guidance for complex tasks"
        COMPREHENSIVE -> "Strong guidance with good efficiency"
        BALANCED -> "Best balance of quality and efficiency"
        STREAMLINED -> "Efficient core behaviors only"
        MINIMAL -> "Maximum efficiency, minimal guidance"
    }
}
