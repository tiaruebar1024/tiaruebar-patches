package app.paresh.patches.eatventure.integrity

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.string

// Returns the SHA1 fingerprint of the app's signing cert, sent to Google
// as X-Android-Cert header in every Firebase App Check request.
// After Morphe re-signs the APK, this returns the wrong cert hash and
// Play Integrity attestation fails. We patch it to return the original hash.
object GetFingerprintHashForPackageFingerprint : Fingerprint(
    definingClass = "Lcom/google/firebase/appcheck/internal/NetworkClient;",
    name = "getFingerprintHashForPackage",
    returnType = "Ljava/lang/String;",
    parameters = listOf()
)

// Installs the AppCheckProviderFactory used to obtain tokens.
// The Unity C# code installs PlayIntegrityAppCheckProviderFactory here.
// We intercept and replace it with DebugAppCheckProviderFactory which
// exchanges a simple UUID token instead of a Play Integrity attestation.
// Used as a secondary fallback if the cert spoof alone is not enough.
object InstallAppCheckProviderFactoryFingerprint : Fingerprint(
    definingClass = "Lcom/google/firebase/appcheck/internal/DefaultFirebaseAppCheck;",
    name = "installAppCheckProviderFactory",
    returnType = "V",
    parameters = listOf("Lcom/google/firebase/appcheck/AppCheckProviderFactory;")
)
