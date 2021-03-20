package io.github.sds100.keymapper

import android.content.Context
import io.github.sds100.keymapper.domain.actions.GetActionErrorUseCaseImpl
import io.github.sds100.keymapper.domain.actions.TestActionUseCaseImpl
import io.github.sds100.keymapper.domain.constraints.GetConstraintErrorUseCaseImpl
import io.github.sds100.keymapper.domain.constraints.IsConstraintSupportedByDeviceUseCaseImpl
import io.github.sds100.keymapper.domain.devices.ShowDeviceInfoUseCaseImpl
import io.github.sds100.keymapper.domain.mappings.keymap.ListKeymapsUseCaseImpl
import io.github.sds100.keymapper.domain.usecases.OnboardingUseCaseImpl

/**
 * Created by sds100 on 03/03/2021.
 */
object UseCases {

    fun getActionError(ctx: Context) = GetActionErrorUseCaseImpl(
        ServiceLocator.packageManagerAdapter(ctx),
        ServiceLocator.inputMethodAdapter(ctx),
        ServiceLocator.permissionAdapter(ctx),
        ServiceLocator.systemFeatureAdapter(ctx),
        ServiceLocator.cameraAdapter(ctx)
    )

    fun getConstraintError(ctx: Context) = GetConstraintErrorUseCaseImpl(
        ServiceLocator.packageManagerAdapter(ctx),
        ServiceLocator.permissionAdapter(ctx),
        ServiceLocator.systemFeatureAdapter(ctx),
    )

    fun testAction(ctx: Context) = TestActionUseCaseImpl()

    fun onboarding(ctx: Context) = OnboardingUseCaseImpl(ServiceLocator.preferenceRepository(ctx))

    fun showDeviceInfo(ctx: Context) = ShowDeviceInfoUseCaseImpl(
        ServiceLocator.deviceInfoRepository(ctx),
        ServiceLocator.preferenceRepository(ctx),
        ServiceLocator.externalDeviceAdapter(ctx)
    )

    fun recordTrigger(ctx: Context) =
        (ctx.applicationContext as MyApplication).recordTriggerController

    fun listKeymaps(ctx: Context) = ListKeymapsUseCaseImpl(
        ServiceLocator.roomKeymapRepository(ctx)
    )

    fun isConstraintSupported(ctx: Context) =
        IsConstraintSupportedByDeviceUseCaseImpl(
            ServiceLocator.systemFeatureAdapter(ctx)
        )
}