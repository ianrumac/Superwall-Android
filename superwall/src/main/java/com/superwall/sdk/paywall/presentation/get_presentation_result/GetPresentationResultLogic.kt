package com.superwall.sdk.paywall.presentation.get_presentation_result

import com.superwall.sdk.models.triggers.InternalTriggerResult
import com.superwall.sdk.paywall.presentation.result.PresentationResult

enum class GetPresentationResultLogic {
    ;
    companion object {
        fun convertTriggerResult(triggerResult: InternalTriggerResult): PresentationResult {
            return when (triggerResult) {
                is InternalTriggerResult.EventNotFound -> PresentationResult.EventNotFound()
                is InternalTriggerResult.Holdout -> PresentationResult.Holdout(triggerResult.experiment)
                is InternalTriggerResult.Error -> PresentationResult.PaywallNotAvailable()
                is InternalTriggerResult.NoRuleMatch -> PresentationResult.NoRuleMatch()
                is InternalTriggerResult.Paywall -> PresentationResult.Paywall(triggerResult.experiment)
            }
        }
    }
}