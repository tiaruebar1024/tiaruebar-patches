package app.paresh.patches.mysterium.shared

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

object Constants {
    val COMPATIBILITY_MYSTERIUM = Compatibility(
        name = "Mysterium VPN",
        packageName = "network.mysterium.vpn",
        apkFileType = ApkFileType.APK,
        appIconColor = 0x6200EE,
        targets = listOf(
            AppTarget(version = "2.1.14")
        )
    )
}
