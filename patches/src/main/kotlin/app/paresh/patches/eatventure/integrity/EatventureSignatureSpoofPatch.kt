package app.paresh.patches.eatventure.integrity

import app.morphe.patcher.patch.resourcePatch
import app.morphe.patcher.patch.bytecodePatch
import app.paresh.patches.eatventure.shared.Constants.COMPATIBILITY_EATVENTURE
import org.w3c.dom.Element

private const val ORIGINAL_APP_CLASS = "com.safedk.android.SafeDKApplication"
private const val SPOOF_APP_CLASS = "app.paresh.extension.eatventure.SpoofSignatureApplication"

private val spoofSignatureResourcePatch = resourcePatch {
    execute {
        // Replace android:name in the <application> tag with our spoof class.
        // SpoofSignatureApplication extends SafeDKApplication so the original
        // initialisation chain is preserved.
        document("AndroidManifest.xml").use { doc ->
            val app = doc.getElementsByTagName("application").item(0) as Element
            val current = app.getAttribute("android:name")
            if (current == ORIGINAL_APP_CLASS) {
                app.setAttribute("android:name", SPOOF_APP_CLASS)
            }
        }
    }
}

@Suppress("unused")
val eatventureSignatureSpoofPatch = bytecodePatch(
    name = "Eatventure Signature Spoof",
    description = "Spoofs the APK signing certificate so Google Play Billing accepts the re-signed APK."
) {
    compatibleWith(COMPATIBILITY_EATVENTURE)
    dependsOn(spoofSignatureResourcePatch)
    extendWith("extensions/extension.mpe")

    execute {
        // Fix the superclass of SpoofSignatureApplication from Application → SafeDKApplication.
        // The extension compiles against android.app.Application but at runtime it must
        // extend SafeDKApplication so the original app initialisation chain is preserved.
        val spoofClass = mutableClassDefBy {
            it.type == "Lapp/paresh/extension/eatventure/SpoofSignatureApplication;"
        }
        spoofClass.setSuperClass("Lcom/safedk/android/SafeDKApplication;")
    }
}
