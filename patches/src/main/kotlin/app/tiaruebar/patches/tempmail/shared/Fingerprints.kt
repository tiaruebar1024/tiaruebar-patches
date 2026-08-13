package app.tiaruebar.patches.tempmail.shared

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import app.morphe.patcher.opcode
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

/**
 * AppUtils.y(Context) — the universal "is free user" gate.
 *
 * Returns true if the SID (subscription ID) stored in SharedPreferences is empty,
 * meaning the user is on the free tier. Called before every ad load/show method
 * in AdManager (9+ call sites) and also drives premium feature gates across the app.
 *
 * Smali (classes3.dex):
 *   .method public final y(Landroid/content/Context;)Z
 *     sget-object v0, Lcom/tempmail/utils/SharedPreferenceHelper;->a:...
 *     invoke-virtual {v0, p1}, Lcom/tempmail/utils/SharedPreferenceHelper;->t(Landroid/content/Context;)Ljava/lang/String;
 *     invoke-static {p1}, Landroid/text/TextUtils;->isEmpty(Ljava/lang/CharSequence;)Z
 *     return p1
 *   .end method
 */
object IsFreeUserFingerprint : Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf("Landroid/content/Context;"),
    filters = listOf(
        methodCall(definingClass = "Lcom/tempmail/utils/SharedPreferenceHelper;", name = "t"),
        methodCall(definingClass = "Landroid/text/TextUtils;", name = "isEmpty"),
    )
)

/**
 * SignatureCheck.verifyIntegrity(Context) — Pairip local signature check.
 *
 * Computes SHA-256 of the APK signature and throws SignatureTamperedException
 * if it doesn't match the expected value. Will crash any re-signed APK without this patch.
 *
 * Non-obfuscated class — matched by exact name.
 * Smali: classes.dex → com/pairip/SignatureCheck.smali
 */
object SignatureCheckFingerprint : Fingerprint(
    definingClass = "Lcom/pairip/SignatureCheck;",
    name = "verifyIntegrity",
    returnType = "V",
    parameters = listOf("Landroid/content/Context;"),
)

/**
 * LicenseClient.processResponse(int, Bundle) — Pairip Play Integrity server check.
 *
 * Called by the Play Integrity service callback. If responseCode != 0, starts
 * the paywall activity or exits the app. Overriding p1 to 0 at entry makes it
 * always take the LICENSED path.
 *
 * Non-obfuscated class — matched by exact name.
 * Smali: classes2.dex → com/pairip/licensecheck/LicenseClient.smali
 */
object ProcessLicenseResponseFingerprint : Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseClient;",
    name = "processResponse",
    returnType = "V",
    parameters = listOf("I", "Landroid/os/Bundle;"),
    accessFlags = listOf(AccessFlags.PRIVATE),
)

/**
 * LicenseClient.startPaywallActivity(PendingIntent) — launches Play Store paywall.
 *
 * Called by processResponse() when responseCode == 2 (NOT_LICENSED). Even though
 * we patch processResponse() to force responseCode to 0, the server-side Pairip
 * check still returns NOT_LICENSED due to signature mismatch, which triggers this.
 *
 * Blocking this method prevents the Play Store "not official app" redirect.
 *
 * Smali: classes2.dex → com/pairip/licensecheck/LicenseClient.smali
 */
object StartPaywallActivityFingerprint : Fingerprint(
    returnType = "V",
    accessFlags = listOf(AccessFlags.PRIVATE),
    parameters = listOf("Landroid/app/PendingIntent;"),
    filters = listOf(
        // Calls createCloseAppIntentOrExitIfAppInBackground()
        methodCall(
            definingClass = "Lcom/pairip/licensecheck/LicenseClient;",
            name = "createCloseAppIntentOrExitIfAppInBackground",
            returnType = "Landroid/content/Intent;"
        ),
        // Puts "paywallintent" extra
        string("paywallintent"),
        methodCall(
            definingClass = "Landroid/content/Intent;",
            name = "putExtra",
            parameters = listOf("Ljava/lang/String;", "Landroid/os/Parcelable;"),
            returnType = "Landroid/content/Intent;"
        )
    )
)

/**
 * AdsIntegrityGate.g() — Play Integrity check refresh at app startup.
 *
 * Called from ApplicationClass.onCreate() to launch a coroutine that checks
 * Play Integrity API. If the check fails (app is tampered/re-signed), the app
 * redirects to Play Store with "Get this app from Play" message.
 *
 * Blocking this method prevents the integrity check from running at all.
 *
 * Non-obfuscated class — matched by exact name.
 * Smali: classes.dex → com/tempmail/data/data_source/integrity/AdsIntegrityGate.smali
 */
object AdsIntegrityGateRefreshFingerprint : Fingerprint(
    definingClass = "Lcom/tempmail/data/data_source/integrity/AdsIntegrityGate;",
    name = "g",
    returnType = "V",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    filters = listOf(
        // Calls Dispatchers.b() to get IO dispatcher
        methodCall(
            definingClass = "Lkotlinx/coroutines/Dispatchers;",
            name = "b",
            returnType = "Lkotlinx/coroutines/CoroutineDispatcher;"
        ),
        // Creates new coroutine instance
        opcode(Opcode.NEW_INSTANCE)
    )
)
