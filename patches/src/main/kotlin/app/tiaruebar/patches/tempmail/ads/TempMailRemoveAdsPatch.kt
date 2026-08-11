package app.tiaruebar.patches.tempmail.ads

import app.morphe.patcher.patch.bytecodePatch
import app.tiaruebar.patches.tempmail.shared.Constants.COMPATIBILITY_TEMP_MAIL
import app.tiaruebar.patches.tempmail.shared.IsFreeUserFingerprint
import app.morphe.util.returnEarly

@Suppress("unused")
val tempMailRemoveAdsPatch = bytecodePatch(
    name = "Temp Mail Remove Ads",
    description = "Removes ads from Temp Mail."
) {
    compatibleWith(COMPATIBILITY_TEMP_MAIL)

    execute {
        // AppUtils.y() returning false means "user is NOT free" = all ad gates skipped.
        // AdManager calls this method before every ad load/show across all 3 ad networks
        // (AdMob, AppLovin MAX, Start.io): banner, native, interstitial, rewarded,
        // rewarded interstitial, and app-open ads are all blocked by this single patch.
        IsFreeUserFingerprint.method.returnEarly(false)
    }
}
