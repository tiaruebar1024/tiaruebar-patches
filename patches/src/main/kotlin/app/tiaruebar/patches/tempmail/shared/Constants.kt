package app.tiaruebar.patches.tempmail.shared

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

object Constants {
    val COMPATIBILITY_TEMP_MAIL = Compatibility(
        name = "Temp Mail",
        packageName = "com.tempmail",
        apkFileType = ApkFileType.APKM,
        appIconColor = 0x14C484,
        targets = listOf(
            AppTarget(version = "4.09")
        )
    )
}
