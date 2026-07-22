package app.paresh.patches.eatventure.iap

import app.morphe.patcher.Fingerprint

// Relays Google Play purchase result to Unity.
object OnPurchaseUpdatedFingerprint : Fingerprint(
    definingClass = "Lcom/unity3d/services/store/WebViewStoreEventListener;",
    name = "onPurchaseUpdated",
    returnType = "V",
    parameters = listOf(
        "Lcom/unity3d/services/store/gpbl/bridges/BillingResultBridge;",
        "Ljava/util/List;"
    )
)

// Called when Unity queries existing purchases on resume.
object OnPurchaseResponseFingerprint : Fingerprint(
    definingClass = "Lcom/unity3d/services/store/WebViewStoreEventListener;",
    name = "onPurchaseResponse",
    returnType = "V",
    parameters = listOf(
        "Lcom/unity3d/services/store/gpbl/bridges/BillingResultBridge;",
        "Ljava/util/List;"
    )
)

// Google Play billing entry point — shows the purchase dialog.
// We intercept to skip the dialog and fire success immediately.
object LaunchBillingFlowFingerprint : Fingerprint(
    definingClass = "Lcom/android/billingclient/api/BillingClientImpl;",
    name = "launchBillingFlow",
    returnType = "Lcom/android/billingclient/api/BillingResult;",
    parameters = listOf(
        "Landroid/app/Activity;",
        "Lcom/android/billingclient/api/BillingFlowParams;"
    )
)

// Billing service setup callback — force always OK so Unity initialises billing.
object OnBillingSetupFinishedFingerprint : Fingerprint(
    definingClass = "Lcom/unity3d/services/store/WebViewStoreEventListener;",
    name = "onBillingSetupFinished",
    returnType = "V",
    parameters = listOf("Lcom/unity3d/services/store/gpbl/bridges/BillingResultBridge;")
)
