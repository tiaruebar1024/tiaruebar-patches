package app.paresh.patches.eatventure.iap

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.paresh.patches.eatventure.shared.Constants.COMPATIBILITY_EATVENTURE

@Suppress("unused")
val eatventureIapPatch = bytecodePatch(
    name = "Eatventure IAP",
    description = "Makes all in-app purchases immediately succeed, for both consumables and permanent items."
) {
    compatibleWith(COMPATIBILITY_EATVENTURE)

    execute {
        // Patch 1: onPurchaseUpdated — fires when user taps Buy.
        // Bypasses the BillingResult.OK check and always sends PURCHASES_UPDATED_RESULT
        // to the Unity game engine, making every purchase appear to succeed.
        // Registers: p0=this, p1=BillingResultBridge, p2=List<PurchaseBridge>
        // v0/v1/v2 are free at method entry (3 registers declared + 2 params = .registers 5).
        OnPurchaseUpdatedFingerprint.method.addInstructions(0, """
            iget-object v0, p0, Lcom/unity3d/services/store/WebViewStoreEventListener;->storeWebViewEventSender:Lcom/unity3d/services/store/StoreWebViewEventSender;
            sget-object v1, Lcom/unity3d/services/store/StoreEvent;->PURCHASES_UPDATED_RESULT:Lcom/unity3d/services/store/StoreEvent;
            new-instance v2, Lorg/json/JSONArray;
            invoke-direct {v2}, Lorg/json/JSONArray;-><init>()V
            filled-new-array {v2}, [Ljava/lang/Object;
            move-result-object v2
            invoke-virtual {v0, v1, v2}, Lcom/unity3d/services/store/StoreWebViewEventSender;->send(Lcom/unity3d/services/store/StoreEvent;[Ljava/lang/Object;)V
            return-void
        """)

        // Patch 2: onPurchaseResponse — fires when Unity queries existing purchases
        // (on app resume and lifecycle events). Skip entirely so Unity never receives
        // a failed purchase-query response that might clear granted items.
        OnPurchaseResponseFingerprint.method.addInstructions(0, """
            return-void
        """)
    }
}
