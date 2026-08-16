package app.tiaruebar.patches.bitlife.ads

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.string
import app.tiaruebar.patches.bitlife.shared.Constants.COMPATIBILITY_BITLIFE
import com.android.tools.smali.dexlib2.AccessFlags

// Generic Ad Loading Fingerprints
object ShowBannerAdFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC),
    strings = listOf("showBannerAds", "banner")
)

object ShowVideoAdFingerprint : Fingerprint(
    strings = listOf("showVideoAd", "video", "reward")
)

@Suppress("unused")
val bitlifeAdRemovalPatch = bytecodePatch(
    name = "BitLife Ad Removal",
    description = "Removes ads from BitLife by disabling ad loading and display methods."
) {
    compatibleWith(COMPATIBILITY_BITLIFE)

    execute {
        // Generic ad methods - return early to skip ad display
        ShowBannerAdFingerprint.methodOrNull?.addInstructions(0, "return-void")
        ShowVideoAdFingerprint.methodOrNull?.addInstructions(0, "return-void")
    }
}