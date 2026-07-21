package app.paresh.patches.device_tycoon.premium

import app.morphe.patcher.patch.rawResourcePatch
import app.paresh.patches.device_tycoon.shared.Constants.COMPATIBILITY_DEVICE_TYCOON

/**
 * Devices Tycoon is a Cordova hybrid app (Construct 3 game engine).
 * All game logic — including IAP and ad handling — lives in JavaScript assets,
 * not Java bytecode. Both patches modify assets/www/scripts/main.js directly.
 *
 * Target 1 (Remove Ads bypass):
 *   The mobileiap DOM handler class sends a "product-owned" event to the game
 *   runtime via _ToProductInfo(). We patch _ToProductInfo() to always return
 *   owned=true for the "remove_ads" product ID.
 *
 * Target 2 (Ad removal):
 *   The advert DOM handler class picks which ad backend to use via _GetApi().
 *   On a real Cordova device it returns C3MobileAdvertsAPI.real (real AdMob).
 *   We patch _GetApi() to always return C3MobileAdvertsAPI.fake — a no-op stub
 *   that immediately reports success for all ad calls without showing any ads.
 */
@Suppress("unused")
val deviceTycoonPremiumPatch = rawResourcePatch(
    name = "Devices Tycoon Premium",
    description = "Unlocks remove ads purchase and disables all ads in Devices Tycoon."
) {
    compatibleWith(COMPATIBILITY_DEVICE_TYCOON)

    execute {
        val mainJs = get("assets/www/scripts/main.js")
        var content = mainJs.readText()

        // --- Patch 1: Remove Ads IAP bypass ---
        // _ToProductInfo() in the mobileiap DOM handler converts a CdvPurchase product
        // object into a plain object sent to the game runtime. We make it report
        // owned=true for the "remove_ads" product regardless of actual purchase state.
        val iapOriginal = "owned:!!e.owned,canPurchase:!!e.canPurchase"
        val iapPatched  = """owned:e.id==="remove_ads"?!0:!!e.owned,canPurchase:!!e.canPurchase"""
        check(content.contains(iapOriginal)) {
            "IAP patch: could not find target string in main.js — app may have updated"
        }
        content = content.replace(iapOriginal, iapPatched)

        // --- Patch 2: Ad removal ---
        // _GetApi() in the advert DOM handler selects the ad backend at runtime.
        // Original: returns C3MobileAdvertsAPI.real when running on a Cordova device.
        // Patched:  always returns C3MobileAdvertsAPI.fake (no-op stub that reports
        //           success instantly and never shows real ads).
        val adOriginal = "return self.cordova?self.C3MobileAdvertsAPI.real:self.cordova||\$t?\$t?self.C3MobileAdvertsAPI.fake:void 0:self.C3MobileAdvertsAPI.web"
        val adPatched  = "return self.C3MobileAdvertsAPI.fake"
        check(content.contains(adOriginal)) {
            "Ad patch: could not find _GetApi() target string in main.js — app may have updated"
        }
        content = content.replace(adOriginal, adPatched)

        mainJs.writeText(content)
    }
}
