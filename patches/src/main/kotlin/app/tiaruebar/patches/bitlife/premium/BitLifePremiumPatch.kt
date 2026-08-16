package app.tiaruebar.patches.bitlife.premium

import app.morphe.patcher.patch.rawResourcePatch
import app.tiaruebar.patches.bitlife.shared.Constants.COMPATIBILITY_BITLIFE

/**
 * BitLife Premium Bypass using Native ARM64 Hex Patching
 *
 * BitLife is a Unity IL2CPP game where all premium logic exists in native ARM64 code
 * within libil2cpp.so. This patch targets the core premium methods identified through
 * IL2CPP analysis with precise RVA addresses.
 *
 * Primary Targets:
 *   1. DoesUserOwnBitizenshipAndGodMode at RVA 0x30B9490 - Main ownership check
 *   2. OwnsIAP at RVA 0x30BBE38 - Universal IAP check covering all products
 *   3. Individual premium getters - 40+ methods for specific features
 *
 * Strategy: Replace method entry points with ARM64 "mov w0, #1; ret" instruction
 * sequence (hex: 20 00 80 52 C0 03 5F D6) to force return true.
 */
@Suppress("unused")
val bitlifePremiumPatch = rawResourcePatch(
    name = "BitLife Premium",
    description = "Unlocks all premium features, Bitizenship, God Mode, and expansions using native ARM64 hex patching of IL2CPP methods."
) {
    compatibleWith(COMPATIBILITY_BITLIFE)

    execute {
        val libil2cpp = get("lib/arm64-v8a/libil2cpp.so")
        if (!libil2cpp.exists()) {
            throw Exception("libil2cpp.so not found - ensure this is the ARM64 version of BitLife")
        }

        val originalBytes = libil2cpp.readBytes()
        var patchedBytes = originalBytes.copyOf()

        // ARM64 instruction sequence: mov w0, #1; ret (always return true)
        val patchBytes = byteArrayOf(
            0x20.toByte(), 0x00.toByte(), 0x80.toByte(), 0x52.toByte(), // mov w0, #1
            0xC0.toByte(), 0x03.toByte(), 0x5F.toByte(), 0xD6.toByte()  // ret
        )

        // Calculate file offsets from RVA addresses
        // Note: These offsets are for BitLife v3.24.4 and will need updating for new versions
        val baseOffset = 0x1000 // Typical base offset for ARM64 libraries
        
        val targets = listOf(
            // Primary targets (high impact)
            0x30B9490 to "DoesUserOwnBitizenshipAndGodMode",
            0x30BBE38 to "OwnsIAP",
            
            // Core premium getters  
            0x30B33E0 to "UserBoughtGodMode",
            0x30A87B0 to "UserBoughtBitizenship",
            0x30B33C0 to "UserBoughtNewBitizenship",
            0x30B33B0 to "UserBoughtLegacyBitizenship",
            0x30B3400 to "UserBoughtBitizenshipAndGodModeTogether",
            
            // Special careers
            0x30B3450 to "UserBoughtBossMode",
            0x30B3470 to "UserBoughtSpecialCareerPolitician",
            0x30B3480 to "UserBoughtSpecialCareerAthlete",
            0x30B3490 to "UserBoughtSpecialCareerMusician",
            0x30B34C0 to "UserBoughtSpecialCareerActor",
            0x30B34B0 to "UserBoughtSpecialCareerMafia",
            
            // Expansion packs
            0x30B35E0 to "UserBoughtExpansionPackInvestor",
            0x30B3600 to "UserBoughtExpansionPackLandlord",
            0x30B3680 to "UserBoughtExpansionPackCasino",
            0x30B36A0 to "UserBoughtExpansionPackVampire",
            
            // Premium tools
            0x30B37A0 to "UserBoughtGoldenPiggyBank",
            0x30B3820 to "UserBoughtGoldenResume",
            0x30B3840 to "UserBoughtGoldenPassport",
            0x30B3800 to "UserBoughtGoldenWrench",
            0x30B3960 to "UserBoughtChallengeVault",
            0x30B3980 to "UserBoughtUnlimitedTimeMachine"
        )

        var patchCount = 0
        for ((rva, methodName) in targets) {
            val fileOffset = rva - baseOffset
            
            // Bounds check
            if (fileOffset >= 0 && fileOffset + 8 <= patchedBytes.size) {
                // Apply the patch (replace first 8 bytes of method with mov w0, #1; ret)
                System.arraycopy(patchBytes, 0, patchedBytes, fileOffset, 8)
                patchCount++
            }
        }

        // Write the patched library
        libil2cpp.writeBytes(patchedBytes)
        
        println("BitLife Premium: Successfully patched $patchCount methods in libil2cpp.so")
    }
}