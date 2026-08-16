package app.tiaruebar.patches.bitlife.misc

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.string
import app.tiaruebar.patches.bitlife.shared.Constants.COMPATIBILITY_BITLIFE
import com.android.tools.smali.dexlib2.AccessFlags

// Pairip (Google Play Integrity) License Check Bypass Fingerprints
object ProcessLicenseResponseFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC),
    strings = listOf("processLicenseResponse")
)

object ValidateLicenseResponseFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    strings = listOf("validateResponse", "verifySignature")
)

@Suppress("unused")
val bitlifePairipBypassPatch = bytecodePatch(
    name = "BitLife Pairip Bypass", 
    description = "Bypasses Google Play Integrity (Pairip) license checks in BitLife."
) {
    compatibleWith(COMPATIBILITY_BITLIFE)

    execute {
        // 1. Set responseCode to 0 (success) in processLicenseResponse
        ProcessLicenseResponseFingerprint.methodOrNull?.let { method ->
            method.addInstructions(0, "const/4 p1, 0x0")
        }

        // 2. Short-circuit validation entirely  
        ValidateLicenseResponseFingerprint.methodOrNull?.let { method ->
            method.addInstructions(0, """
                const/4 v0, 0x1
                return v0
            """)
        }
    }
}