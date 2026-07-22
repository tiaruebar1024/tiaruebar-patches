package app.paresh.patches.eatventure.iap

import app.morphe.patcher.Fingerprint

// Relays Google Play purchase result to Unity game engine.
// Called when user completes (or cancels) a purchase flow.
// Non-obfuscated Unity SDK class — safe to use definingClass + name.
object OnPurchaseUpdatedFingerprint : Fingerprint(
    definingClass = "Lcom/unity3d/services/store/WebViewStoreEventListener;",
    name = "onPurchaseUpdated",
    returnType = "V",
    parameters = listOf(
        "Lcom/unity3d/services/store/gpbl/bridges/BillingResultBridge;",
        "Ljava/util/List;"
    )
)

// Called when Unity queries existing purchases (on app resume / lifecycle).
// Patching this ensures restored-purchases queries also report success.
object OnPurchaseResponseFingerprint : Fingerprint(
    definingClass = "Lcom/unity3d/services/store/WebViewStoreEventListener;",
    name = "onPurchaseResponse",
    returnType = "V",
    parameters = listOf(
        "Lcom/unity3d/services/store/gpbl/bridges/BillingResultBridge;",
        "Ljava/util/List;"
    )
)
