package app.paresh.patches.device_tycoon.premium

import app.morphe.patcher.patch.rawResourcePatch
import app.paresh.patches.device_tycoon.shared.Constants.COMPATIBILITY_DEVICE_TYCOON

/**
 * Devices Tycoon is a Cordova hybrid app (Construct 3 game engine).
 * All game logic — including IAP and ad handling — lives in JavaScript assets,
 * not Java bytecode. Both patches modify assets/www/scripts/main.js directly.
 *
 * IAP products:
 *   Non-consumable: remove_ads
 *   Consumable:     get_1000_researchcoins_v2, get_3000_researchcoins,
 *                   get_5000_researchcoins, get_300m_budget, get_750m_budget,
 *                   get_1b_budget
 *
 * Patch 1 — Remove Ads bypass (non-consumable):
 *   _ToProductInfo() reports owned=true for remove_ads so the game's
 *   ProductOwned("remove_ads") condition always passes.
 *
 * Patch 2 — Purchase bypass (all products):
 *   _OnPurchase(productId) normally calls store.order() to start a real Google
 *   Play purchase flow. We replace it so it skips the store entirely and
 *   immediately calls _OnProductOwned() + _OnPurchaseSuccess(), exactly as if
 *   the purchase completed successfully. This fires the game's OnProductOwned /
 *   OnAnyProductOwned triggers the moment the player taps Buy — granting coins,
 *   budget, or ad removal instantly with no real payment.
 *
 * Patch 3 — Ad removal:
 *   _GetApi() always returns C3MobileAdvertsAPI.fake — a no-op stub that reports
 *   success instantly without showing any real ads.
 */
@Suppress("unused")
val deviceTycoonPremiumPatch = rawResourcePatch(
    name = "Devices Tycoon Premium",
    description = "Instantly completes all IAP purchases (remove ads, research coins, budget) on tap and disables all ads in Devices Tycoon."
) {
    compatibleWith(COMPATIBILITY_DEVICE_TYCOON)

    execute {
        val mainJs = get("assets/www/scripts/main.js")
        var content = mainJs.readText()

        // --- Patch 1: Remove Ads IAP bypass (non-consumable) ---
        // Force owned=true for remove_ads in _ToProductInfo() so the game's
        // ProductOwned("remove_ads") condition always evaluates to true.
        val iapOriginal = "owned:!!e.owned,canPurchase:!!e.canPurchase"
        val iapPatched  = """owned:e.id==="remove_ads"?!0:!!e.owned,canPurchase:!!e.canPurchase"""
        check(content.contains(iapOriginal)) {
            "Patch 1: could not find _ToProductInfo target in main.js — app may have updated"
        }
        content = content.replace(iapOriginal, iapPatched)

        // --- Patch 2: Purchase bypass (all products) ---
        // Original _OnPurchase calls store.order() which triggers a real Play Store
        // purchase dialog. We replace the entire method body to skip the store and
        // immediately fire _OnProductOwned() + _OnPurchaseSuccess() on the product,
        // which is exactly what the real flow does after a successful transaction.
        // The player taps Buy → instant success, no payment required.
        //
        // We match on the stable prefix up to store.order() and the stable suffix,
        // avoiding the backtick template literal inside console.log which is
        // awkward to embed in a Kotlin string literal.
        val purchaseOrderCall = "this._store.order(t.getOffer()).then((e=>{e&&e.isError?this._OnPurchaseFail(t,e):this._OnPurchaseSuccess(t)}))"
        check(content.contains(purchaseOrderCall)) {
            "Patch 2: could not find store.order() call in main.js — app may have updated"
        }
        // Replace just the store.order(...) call with a direct success — the surrounding
        // if(t) block stays intact so the product lookup still runs first.
        content = content.replace(
            purchaseOrderCall,
            "this._OnProductOwned(t),this._OnPurchaseSuccess(t)"
        )

        // --- Patch 3: Ad removal ---
        // Always return the fake/stub ad API so no real ads are loaded or shown.
        val adOriginal = "return self.cordova?self.C3MobileAdvertsAPI.real:self.cordova||\$t?\$t?self.C3MobileAdvertsAPI.fake:void 0:self.C3MobileAdvertsAPI.web"
        val adPatched  = "return self.C3MobileAdvertsAPI.fake"
        check(content.contains(adOriginal)) {
            "Patch 3: could not find _GetApi() target in main.js — app may have updated"
        }
        content = content.replace(adOriginal, adPatched)

        mainJs.writeText(content)
    }
}
