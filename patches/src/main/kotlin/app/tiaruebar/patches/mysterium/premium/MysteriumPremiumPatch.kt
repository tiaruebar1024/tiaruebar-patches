package app.tiaruebar.patches.mysterium.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.tiaruebar.patches.mysterium.shared.Constants.COMPATIBILITY_MYSTERIUM

@Suppress("unused")
val mysteriumPremiumPatch = bytecodePatch(
    name = "Mysterium VPN Premium",
    description = "Removes balance restrictions in Mysterium VPN."
) {
    compatibleWith(COMPATIBILITY_MYSTERIUM)

    execute {
        // Target 1: Stop the auto-disconnect callback that fires when balance <= 0.0001 MYST.
        BalanceAutoDisconnectFingerprint.method.addInstructions(0, "return-void")

        // Target 2: Skip the pre-connection "insufficient funds" popup when balance == 0.
        CheckAbilityToConnectFingerprint.method.addInstructions(0, "return-void")

        // Target 3: Skip the "insufficient funds" popup shown on connection failure
        // when balance < 0.0001 MYST (shows "try again" dialog instead).
        FailedToConnectBalanceCheckFingerprint.method.addInstructions(0, "return-void")

        // Target 4a: Return true for isTopFlowShown() so new users bypass the
        // mandatory top-up onboarding screen on first launch.
        IsTopUpFlowShownFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """
        )

        // Target 4b: Return true for isAccountCreated() so the splash screen
        // does not redirect to CreateAccountActivity on first launch.
        IsAccountCreatedFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """
        )
    }
}
