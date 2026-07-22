package app.paresh.extension.eatventure;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.Signature;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import java.lang.reflect.Field;

/**
 * Hooks PackageInfo.CREATOR at the Parcel level to spoof the APK signing cert.
 * Google Play Billing verifies the APK signature via getPackageInfo(GET_SIGNATURES).
 * After Morphe re-signs the APK the cert changes, causing "not configured for billing".
 * This intercept runs before any SDK reads signatures, restoring the original cert.
 *
 * NOTE: The actual superclass is set to SafeDKApplication by the bytecode patch
 * at patch time, so the original app initialisation chain is preserved at runtime.
 * We extend Application here only to satisfy the compiler.
 */
@SuppressWarnings("unused")
public class SpoofSignatureApplication extends Application {

    private static final String TAG = "SpoofSig";
    private static final String PKG = "com.hwqgrhhjfd.idlefastfood";

    // Original APK signing certificate DER (SHA1: EB63465E32D9C25AB042CD2B96EAE5A9D8D1CA65)
    private static final String CERT_HEX =
        "3082058930820371a003020102021500c85e1b7bcb2cade8109637be0f90d1e6b444b3b0300d06092a864886f7" +
        "0d01010b05003074310b3009060355040613025553311330110603550408130a43616c69666f726e6961311630" +
        "140603550407130d4d6f756e7461696e205669657731143012060355040a130b476f6f676c6520496e632e3110" +
        "300e060355040b1307416e64726f69643110300e06035504031307416e64726f69643020170d32313038313131" +
        "35333732345a180f32303531303831313135333732345a3074310b300906035504061302555331133011060355" +
        "0408130a43616c69666f726e6961311630140603550407130d4d6f756e7461696e205669657731143012060355" +
        "040a130b476f6f676c6520496e632e3110300e060355040b1307416e64726f69643110300e0603550403130741" +
        "6e64726f696430820222300d06092a864886f70d01010105000382020f003082020a0282020100afbc47b83088" +
        "ce50a218b5d740ae1329ed4ebd78fa131f8a82dd0214c840ef1dd5ec3a9c50dd1d6e007ba9a6d57080a86ce4de" +
        "e52737a91ee9b0d4f1dec4ef17fb8efd592d33e4001c8fbb2f57eaa41220b96d3b2c7337a7516f4c06312f8d62" +
        "b339ff3bf3dc3d7b6653f1687b2d1cda4c4dd9711f50976c98c17f549ffe6bc9978873583d73f1f8101c1478c3" +
        "a0539b9bc10370fc8af12f1bd3ba1dd490a77d083b20545aa7b54f21cee412f6f86d8252c42e1ae532a09bc393" +
        "8c2aeea728c230eec91bda0d5465ba71f6f13d003036bb6e31251e083cf30107b23ae075d3ce6fb7afe33c8730" +
        "f28942fae78463b957f5680993385c7c1414da69c487bd882b43d2e29a98d4d7c6ad96f4e0e3005ade5bdb244c" +
        "70d096535bbacbae3e74d48571a6e6837c2f711da7c9290d53daff903c616035a76d26fe6110917adf0a842b70" +
        "4577fc3ed22d9b42f49a477f34dc968a7a73bda278534e14b567826353900a3567ce0f5db6817a75e20cee744a" +
        "d3a71fe646689aec355d2d8d993ec534e268959ccc4d28e101ec8ed481dab01e939672cf03a6e00bac2baf2766" +
        "fdff70b2ec011509509f96fbddba6f5d43c5e57ceb35137190fc09c199493239b5f46c9c39fa3b5140942aeec5" +
        "a3119e21bb864cccdb492dccdd9d05ea4a05278bd33c469b79a11bc4496bf0e259c8976d4ea8a9bf3b281772e7" +
        "cf3a35bcfc2f3121d5e6530203010001a310300e300c0603551d13040530030101ff300d06092a864886f70d01" +
        "010b0500038202010025d868fe9ca13d6a066ae3b823a4de7254e6644f337836d24eebdf98984bc8967666dcc0" +
        "287df12b8cd9158ac84a91f641fb00c153615d5d72207844cce02ffd75633db3b5b11c20a2e46d968c211b7f10" +
        "44b4a2a5bda609e3645d8569132027d062b43997190a93f85624795affae67aaa1fa50f24ad735a32edc0f4500" +
        "810f88167c557c953d0e29bc986ceee773b898fab154744140fa7d74ada77e53f0e74f42c7fb4bfb1a286203cf" +
        "907a32bfc8dce8a83e03df81b249768c79b03cc1771ae315c0b3fcb21369cacd03e3e8bd3dc5315ae6813e8301" +
        "cb11aacb975a36f2e785877cc6a9fd2d5bba35019b35ad21583c54dc9d6eafc0caf3e83b32e41bbb1c10bbde2d" +
        "8340889620690060362ae2f2db54367670dc2ec9659030e09293ba35ef62dad72fd6560b5ba8eb42c17a64c6c6" +
        "1c4d7a66207af25812e90c07571df34bd6cedc0cc310c5dafdb1217120181f6c5e05d0451a8bd7213dfc112523" +
        "eedf79201ac4e974aff230c92d8dd1dd566d1e44d62426ce05ec464d55a825cd367d1c3d36fff0117686104ad5" +
        "267b894810ecb73f8285ca185877e15105cea66bdc5cc6739d1f571035be909bcc5ec999850e59626e6df3a484" +
        "d166e75b30f98753d3b1ad7deda0148a4820671b4ec3f485e9b7f7bd3ede2ddc300c0ff046840aa3a1115a17a6" +
        "e70951a675c22648d1a673f47eb2791506c2cd4e4964edfb7ab3";

    static {
        installHook();
    }

    private static void installHook() {
        try {
            final byte[] certDer = hexToBytes(CERT_HEX);
            final Signature spoofed = new Signature(certDer);

            @SuppressWarnings("unchecked")
            final Parcelable.Creator<PackageInfo> original =
                    (Parcelable.Creator<PackageInfo>) getField(PackageInfo.class, null, "CREATOR");

            Parcelable.Creator<PackageInfo> hooked = new Parcelable.Creator<PackageInfo>() {
                @Override public PackageInfo createFromParcel(Parcel src) {
                    PackageInfo info = original.createFromParcel(src);
                    if (info != null && PKG.equals(info.packageName)) {
                        info.signatures = new Signature[]{spoofed};
                    }
                    return info;
                }
                @Override public PackageInfo[] newArray(int size) {
                    return original.newArray(size);
                }
            };

            setField(PackageInfo.class, null, "CREATOR", hooked);
            clearCache(Parcel.class, "mCreators");
            clearCache(Parcel.class, "sPairedCreators");
            Log.d(TAG, "Signature hook installed");
        } catch (Throwable t) {
            Log.w(TAG, "Hook failed: " + t);
        }
    }

    private static Object getField(Class<?> cls, Object obj, String name) throws Exception {
        Field f = cls.getDeclaredField(name);
        f.setAccessible(true);
        return f.get(obj);
    }

    private static void setField(Class<?> cls, Object obj, String name, Object val) throws Exception {
        Field f = cls.getDeclaredField(name);
        f.setAccessible(true);
        f.set(obj, val);
    }

    private static void clearCache(Class<?> cls, String name) {
        try {
            Field f = cls.getDeclaredField(name);
            f.setAccessible(true);
            Object map = f.get(null);
            if (map != null) map.getClass().getMethod("clear").invoke(map);
        } catch (Throwable ignored) {}
    }

    private static byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] out = new byte[len / 2];
        for (int i = 0; i < len; i += 2)
            out[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    | Character.digit(hex.charAt(i + 1), 16));
        return out;
    }
}
