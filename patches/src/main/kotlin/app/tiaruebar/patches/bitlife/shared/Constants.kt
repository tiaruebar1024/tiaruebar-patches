package app.tiaruebar.patches.bitlife.shared

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

object Constants {
    val COMPATIBILITY_BITLIFE = Compatibility(
        name = "BitLife",
        packageName = "com.candywriter.bitlife",
        apkFileType = ApkFileType.APK,
        appIconColor = 0xFF6B35,
        targets = listOf(
            AppTarget(version = "3.24.4")
        )
    )
}