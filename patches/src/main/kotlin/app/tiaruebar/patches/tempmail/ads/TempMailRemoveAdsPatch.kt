package app.tiaruebar.patches.tempmail.ads

import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.util.returnConst
import app.tiaruebar.patches.tempmail.shared.Constants.COMPATIBILITY_TEMP_MAIL
import app.tiaruebar.patches.tempmail.shared.IsFreeUserFingerprint

// Helper function right inside this file:
private fun app.morphe.patcher.fingerprint.MethodFingerprint.returnEarly(value: Boolean) {
    this.returnConst(value)
}

@Suppress("unused")
val tempMailRemoveAdsPatch = bytecodePatch(
    name = "Temp Mail Remove Ads",
    description = "Removes ads from Temp Mail."
) {
    compatibleWith(COMPATIBILITY_TEMP_MAIL)

    execute {
        IsFreeUserFingerprint.method.returnEarly(false)
    }
}
