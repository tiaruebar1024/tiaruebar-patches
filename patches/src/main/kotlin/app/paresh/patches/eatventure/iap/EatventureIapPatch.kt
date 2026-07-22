package app.paresh.patches.eatventure.iap

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.paresh.patches.eatventure.shared.Constants.COMPATIBILITY_EATVENTURE

@Suppress("unused")
val eatventureIapPatch = bytecodePatch(
    name = "Eatventure IAP",
    description = "Skips the Google Play purchase dialog and auto-succeeds all purchases."
) {
    compatibleWith(COMPATIBILITY_EATVENTURE)

    execute {
        // Patch 1: Skip the Google Play purchase dialog entirely.
        // BillingClientImpl.launchBillingFlow() is called by libil2cpp.so when the
        // user taps a buy button. Normally it shows the Google Play dialog.
        // We skip all that and:
        //   1. Build a BillingResult with responseCode=0 (OK)
        //   2. Get the PurchasesUpdatedListener from this.zzf.zzd()
        //   3. Call onPurchasesUpdated(OK, null) — Unity treats null list same as empty
        //   4. Return the OK BillingResult
        //
        // Register layout at entry: p0=this(BillingClientImpl), p1=Activity, p2=BillingFlowParams
        // .registers 33 — v0..v30 available
        LaunchBillingFlowFingerprint.method.addInstructions(0, """
            invoke-static {}, Lcom/android/billingclient/api/BillingResult;->newBuilder()Lcom/android/billingclient/api/BillingResult${'$'}Builder;
            move-result-object v0
            const/4 v1, 0x0
            invoke-virtual {v0, v1}, Lcom/android/billingclient/api/BillingResult${'$'}Builder;->setResponseCode(I)Lcom/android/billingclient/api/BillingResult${'$'}Builder;
            invoke-virtual {v0}, Lcom/android/billingclient/api/BillingResult${'$'}Builder;->build()Lcom/android/billingclient/api/BillingResult;
            move-result-object v0
            iget-object v1, p0, Lcom/android/billingclient/api/BillingClientImpl;->zzf:Lcom/android/billingclient/api/zzs;
            if-eqz v1, :no_listener
            invoke-virtual {v1}, Lcom/android/billingclient/api/zzs;->zzd()Lcom/android/billingclient/api/PurchasesUpdatedListener;
            move-result-object v1
            if-eqz v1, :no_listener
            const/4 v2, 0x0
            invoke-interface {v1, v0, v2}, Lcom/android/billingclient/api/PurchasesUpdatedListener;->onPurchasesUpdated(Lcom/android/billingclient/api/BillingResult;Ljava/util/List;)V
            :no_listener
            return-object v0
        """)

        // Patch 2: Force billing setup to always report OK to Unity.
        // If BillingClient.startConnection() gets BILLING_UNAVAILABLE, Unity refuses to
        // process purchases. We always send INITIALIZATION_REQUEST_RESULT (success).
        // p0=this, p1=BillingResultBridge — .registers 5
        OnBillingSetupFinishedFingerprint.method.addInstructions(0, """
            iget-object v0, p0, Lcom/unity3d/services/store/WebViewStoreEventListener;->storeWebViewEventSender:Lcom/unity3d/services/store/StoreWebViewEventSender;
            sget-object v1, Lcom/unity3d/services/store/StoreEvent;->INITIALIZATION_REQUEST_RESULT:Lcom/unity3d/services/store/StoreEvent;
            sget-object v2, Lcom/unity3d/services/store/gpbl/BillingResultResponseCode;->OK:Lcom/unity3d/services/store/gpbl/BillingResultResponseCode;
            filled-new-array {v2}, [Ljava/lang/Object;
            move-result-object v2
            invoke-virtual {v0, v1, v2}, Lcom/unity3d/services/store/StoreWebViewEventSender;->send(Lcom/unity3d/services/store/StoreEvent;[Ljava/lang/Object;)V
            return-void
        """)

        // Patch 3: onPurchaseUpdated fallback — if launchBillingFlow somehow still fires
        // the real purchase flow, intercept the result and always send success to Unity.
        // p0=this, p1=BillingResultBridge, p2=List — .registers 5
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

        // Patch 4: Skip purchase-query callback on resume (no-op).
        OnPurchaseResponseFingerprint.method.addInstructions(0, "return-void")
    }
}
