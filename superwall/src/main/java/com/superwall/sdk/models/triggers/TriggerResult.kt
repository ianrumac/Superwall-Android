package com.superwall.sdk.models.triggers

import com.superwall.sdk.models.serialization.ExceptionSerializer
import kotlinx.serialization.Serializable

// The result of a paywall trigger.
//
// Triggers can conditionally show paywalls. Contains the possible cases resulting from the trigger.
@Serializable
sealed class TriggerResult {

    // This event was not found on the dashboard.
    //
    // Please make sure you have added the event to a campaign on the dashboard and
    // double check its spelling.
    @Serializable
    object EventNotFound : TriggerResult()

    // No matching rule was found for this trigger so no paywall will be shown.
    @Serializable
    object NoRuleMatch : TriggerResult()

    // A matching rule was found and this user will be shown a paywall.
    //
    // - Parameters:
    //   - experiment: The experiment associated with the trigger.
    @Serializable
    data class Paywall(val experiment: Experiment) : TriggerResult()

    // A matching rule was found and this user was assigned to a holdout group so will not be shown a paywall.
    //
    // - Parameters:
    //   - experiment: The experiment  associated with the trigger.
    @Serializable
    data class Holdout(val experiment: Experiment) : TriggerResult()

    // An error occurred and the user will not be shown a paywall.
    //
    // If the error code is `101`, it means that no view controller could be found to present on. Otherwise a network failure may have occurred.
    //
    // In these instances, consider falling back to a native paywall.
    @Serializable
    data class Error(val error: @Serializable(with = ExceptionSerializer::class) Exception) :
        TriggerResult()
}

/**
 * The result of a paywall trigger. `NoRuleMatch` is an associated sealed class.
 *
 * Triggers can conditionally show paywalls. Contains the possible cases resulting from the trigger.
 */
sealed class InternalTriggerResult {

    /**
     * This event was not found on the dashboard.
     *
     * Please make sure you have added the event to a campaign on the dashboard and
     * double check its spelling.
     */
    object EventNotFound : InternalTriggerResult()

    /**
     * No matching rule was found for this trigger so no paywall will be shown.
     */
    data class NoRuleMatch(val unmatchedRules: List<UnmatchedRule>) : InternalTriggerResult()

    /**
     * A matching rule was found and this user will be shown a paywall.
     *
     * @property experiment The experiment associated with the trigger.
     */
    data class Paywall(val experiment: Experiment) : InternalTriggerResult()

    /**
     * A matching rule was found and this user was assigned to a holdout group so will not be shown a paywall.
     *
     * @property experiment The experiment associated with the trigger.
     */
    data class Holdout(val experiment: Experiment) : InternalTriggerResult()

    /**
     * An error occurred and the user will not be shown a paywall.
     *
     * If the error code is `101`, it means that no view controller could be found to present on. Otherwise a network failure may have occurred.
     *
     * In these instances, consider falling back to a native paywall.
     */
    data class Error(val error: Exception) : InternalTriggerResult()

    fun toPublicType(): TriggerResult = when (this) {
        is EventNotFound -> TriggerResult.EventNotFound
        is NoRuleMatch -> TriggerResult.NoRuleMatch
        is Paywall -> TriggerResult.Paywall(experiment)
        is Holdout -> TriggerResult.Holdout(experiment)
        is Error -> TriggerResult.Error(error)
    }
}


