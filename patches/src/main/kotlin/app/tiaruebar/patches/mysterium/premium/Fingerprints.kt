package app.tiaruebar.patches.mysterium.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.literal
import app.morphe.patcher.methodCall
import com.android.tools.smali.dexlib2.AccessFlags

// Target 1: Auto-disconnects the VPN session when balance drops to <= 0.0001 MYST.
// Class: ConnectionViewModel$balanceListener$1$1, method: invoke(D)V
// Verified in: classes2/updated/mysterium/vpn/ui/connection/ConnectionViewModel$balanceListener$1$1.smali
object BalanceAutoDisconnectFingerprint : Fingerprint(
    definingClass = "Lupdated/mysterium/vpn/ui/connection/ConnectionViewModel\$balanceListener\$1\$1;",
    name = "invoke",
    returnType = "V",
    parameters = listOf("D"),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    filters = listOf(
        // const-wide v0, 0x3f1a36e2eb1c432dL  # 1.0E-4
        literal(0x3f1a36e2eb1c432dL),
        // invoke-virtual ... ConnectionViewModel;->disconnect()
        methodCall(
            definingClass = "Lupdated/mysterium/vpn/ui/connection/ConnectionViewModel;",
            name = "disconnect"
        )
    )
)

// Target 2: Pre-connection balance gate. Shows "insufficient funds" popup before connecting
// when wallet balance == 0. Static lambda of checkAbilityToConnect.
// Class: ConnectionActivity, method: checkAbilityToConnect$lambda-10$lambda-9(ConnectionActivity, Result)V
// Verified in: classes2/updated/mysterium/vpn/ui/connection/ConnectionActivity.smali line 914
object CheckAbilityToConnectFingerprint : Fingerprint(
    definingClass = "Lupdated/mysterium/vpn/ui/connection/ConnectionActivity;",
    accessFlags = listOf(AccessFlags.PRIVATE, AccessFlags.STATIC, AccessFlags.FINAL),
    returnType = "V",
    parameters = listOf(
        "Lupdated/mysterium/vpn/ui/connection/ConnectionActivity;",
        "Lkotlin/Result;"
    ),
    filters = listOf(
        // invoke-static ... Lkotlin/Result;->isSuccess-impl(Ljava/lang/Object;)Z
        methodCall(definingClass = "Lkotlin/Result;", name = "isSuccess-impl"),
        // invoke-virtual ... Ljava/lang/Number;->doubleValue()D
        methodCall(definingClass = "Ljava/lang/Number;", name = "doubleValue"),
        // invoke-virtual ... ConnectionActivity;->insufficientFundsPopUp(...)
        methodCall(
            definingClass = "Lupdated/mysterium/vpn/ui/connection/ConnectionActivity;",
            name = "insufficientFundsPopUp"
        )
    )
)

// Target 3: Failed-to-connect balance gate. Shows "insufficient funds" popup instead of
// "try again" when a connection attempt fails with balance < 0.0001 MYST.
// Class: ConnectionActivity, method: failedToConnect$lambda-34(ConnectionActivity, Result)V
// Verified in: classes2/updated/mysterium/vpn/ui/connection/ConnectionActivity.smali line 1743
object FailedToConnectBalanceCheckFingerprint : Fingerprint(
    definingClass = "Lupdated/mysterium/vpn/ui/connection/ConnectionActivity;",
    accessFlags = listOf(AccessFlags.PRIVATE, AccessFlags.STATIC, AccessFlags.FINAL),
    returnType = "V",
    parameters = listOf(
        "Lupdated/mysterium/vpn/ui/connection/ConnectionActivity;",
        "Lkotlin/Result;"
    ),
    filters = listOf(
        // invoke-static ... Lkotlin/Result;->isSuccess-impl(Ljava/lang/Object;)Z
        methodCall(definingClass = "Lkotlin/Result;", name = "isSuccess-impl"),
        // invoke-virtual ... Ljava/lang/Number;->doubleValue()D
        methodCall(definingClass = "Ljava/lang/Number;", name = "doubleValue"),
        // const-wide v2, 0x3f1a36e2eb1c432dL  # 1.0E-4  (distinguishes from Target 2 which uses 0x0)
        literal(0x3f1a36e2eb1c432dL),
        // invoke-virtual ... ConnectionActivity;->insufficientFundsPopUp(...)
        methodCall(
            definingClass = "Lupdated/mysterium/vpn/ui/connection/ConnectionActivity;",
            name = "insufficientFundsPopUp"
        )
    )
)

// Target 4a: Top-up onboarding wall. Returns whether user has completed the top-up flow.
// If false on first launch, SplashActivity redirects new users to the top-up screen.
// Class: LoginUseCase, method: isTopFlowShown()Z
// Verified in: classes2/updated/mysterium/vpn/network/usecase/LoginUseCase.smali line 169
object IsTopUpFlowShownFingerprint : Fingerprint(
    definingClass = "Lupdated/mysterium/vpn/network/usecase/LoginUseCase;",
    name = "isTopFlowShown",
    returnType = "Z",
    parameters = listOf(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    filters = listOf(
        // sget-object ... SharedPreferencesList;->TOP_UP_FLOW
        methodCall(
            definingClass = "Lupdated/mysterium/vpn/database/preferences/SharedPreferencesManager;",
            name = "containsPreferenceValue"
        )
    )
)

// Target 4b: Account creation wall. Returns whether user has created/imported an account.
// If false on first launch, SplashActivity redirects to CreateAccountActivity.
// Class: LoginUseCase, method: isAccountCreated()Z
// Verified in: classes2/updated/mysterium/vpn/network/usecase/LoginUseCase.smali line 111
object IsAccountCreatedFingerprint : Fingerprint(
    definingClass = "Lupdated/mysterium/vpn/network/usecase/LoginUseCase;",
    name = "isAccountCreated",
    returnType = "Z",
    parameters = listOf(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    filters = listOf(
        // sget-object ... SharedPreferencesList;->ACCOUNT_CREATED
        methodCall(
            definingClass = "Lupdated/mysterium/vpn/database/preferences/SharedPreferencesManager;",
            name = "containsPreferenceValue"
        )
    )
)
