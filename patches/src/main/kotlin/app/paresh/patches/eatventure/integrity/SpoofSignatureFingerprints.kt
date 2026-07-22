package app.paresh.patches.eatventure.integrity

import app.morphe.patcher.Fingerprint

// SafeDKApplication constructor — we need its class type to verify it exists
object SafeDKApplicationFingerprint : Fingerprint(
    definingClass = "Lcom/safedk/android/SafeDKApplication;",
    name = "<init>",
    returnType = "V",
    parameters = listOf()
)
