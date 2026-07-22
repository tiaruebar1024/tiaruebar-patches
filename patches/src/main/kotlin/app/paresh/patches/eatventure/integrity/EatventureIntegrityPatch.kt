package app.paresh.patches.eatventure.integrity

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.paresh.patches.eatventure.shared.Constants.COMPATIBILITY_EATVENTURE

// Original APK signing certificate SHA1 fingerprint (from APK v2 signing block).
// This is what AndroidUtilsLight.getPackageCertificateHashBytes() would return
// for the unmodified APK, hex-encoded uppercase.
private const val ORIGINAL_CERT_SHA1 = "B677ECFF0D9C2BAA2798A768F8336E154D7EB82A"

@Suppress("unused")
val eatventureIntegrityPatch = bytecodePatch(
    name = "Eatventure Play Integrity Bypass",
    description = "Bypasses the Play Integrity / Firebase App Check signature verification that blocks patched APKs."
) {
    compatibleWith(COMPATIBILITY_EATVENTURE)

    execute {
        // Patch 1 (primary): Spoof the cert fingerprint sent to Google.
        //
        // NetworkClient.getFingerprintHashForPackage() reads the APK signing cert
        // via AndroidUtilsLight.getPackageCertificateHashBytes() and hex-encodes it.
        // After Morphe re-signs the APK the hash changes, causing Play Integrity to fail.
        // We return the original cert SHA1 directly, so Google sees the correct hash.
        //
        // Method: private getFingerprintHashForPackage()Ljava/lang/String;
        // .registers 6 — v0..v3 are free at entry, p0=this
        GetFingerprintHashForPackageFingerprint.method.addInstructions(0, """
            const-string v0, "$ORIGINAL_CERT_SHA1"
            return-object v0
        """)

        // Patch 2 (secondary): Replace PlayIntegrity provider with DebugAppCheckProvider.
        //
        // The Unity C# code calls installAppCheckProviderFactory(PlayIntegrityFactory).
        // We intercept p1 (the factory argument) and replace it with
        // DebugAppCheckProviderFactory.getInstance() before it gets stored.
        // The debug provider uses a UUID token exchanged via the debug token endpoint,
        // which bypasses hardware attestation entirely.
        //
        // Method: public installAppCheckProviderFactory(AppCheckProviderFactory)V
        // .registers 3 — p0=this, p1=factory (we replace p1)
        InstallAppCheckProviderFactoryFingerprint.method.addInstructions(0, """
            invoke-static {}, Lcom/google/firebase/appcheck/debug/DebugAppCheckProviderFactory;->getInstance()Lcom/google/firebase/appcheck/debug/DebugAppCheckProviderFactory;
            move-result-object p1
        """)
    }
}
