package app.tiaruebar.patches.tempmail.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.tiaruebar.patches.tempmail.shared.AdsIntegrityGateRefreshFingerprint
import app.tiaruebar.patches.tempmail.shared.Constants.COMPATIBILITY_TEMP_MAIL
import app.tiaruebar.patches.tempmail.shared.IsFreeUserFingerprint
import app.tiaruebar.patches.tempmail.shared.ProcessLicenseResponseFingerprint
import app.tiaruebar.patches.tempmail.shared.SignatureCheckFingerprint
import app.tiaruebar.patches.tempmail.shared.StartPaywallActivityFingerprint

@Suppress("unused")
val tempMailPremiumPatch = bytecodePatch(
    name = "Temp Mail Premium",
    description = "Unlocks premium features in Temp Mail."
) {
    compatibleWith(COMPATIBILITY_TEMP_MAIL)

    execute {
        // AppUtils.y() returns true when SID is empty (= free user).
        // Returning false makes the app always believe the user is premium.
        IsFreeUserFingerprint.method.addInstructions(0, """
            const/4 v0, 0x0
            return v0
        """)

        // Skip the local Pairip signature check so the re-signed APK
        // does not throw SignatureTamperedException on startup.
        SignatureCheckFingerprint.method.addInstructions(0, "return-void")

        // Force the Pairip Play Integrity server response code to 0 (LICENSED)
        // so the license check always succeeds without showing the paywall.
        ProcessLicenseResponseFingerprint.method.addInstruction(0, "const/4 p1, 0x0")

        // Block Play Store redirect — even if server returns NOT_LICENSED (responseCode 2),
        // the paywall activity never launches. Defense-in-depth for signature mismatch.
        StartPaywallActivityFingerprint.method.addInstructions(0, "return-void")

        // Skip Play Integrity check at app startup — prevents "Get this app from Play"
        // redirect when app detects tampering. This is separate from Pairip license check.
        AdsIntegrityGateRefreshFingerprint.method.addInstructions(0, "return-void")
    }
}
