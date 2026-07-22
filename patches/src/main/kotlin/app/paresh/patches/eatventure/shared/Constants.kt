package app.paresh.patches.eatventure.shared

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

object Constants {
    val COMPATIBILITY_EATVENTURE = Compatibility(
        name = "Eatventure",
        packageName = "com.hwqgrhhjfd.idlefastfood",
        apkFileType = ApkFileType.APKM,
        appIconColor = 0xFF6644,
        targets = listOf(
            AppTarget(version = "1.52.3")
        )
    )
}
