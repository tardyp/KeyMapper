package io.github.sds100.keymapper

import android.content.Context
import io.github.sds100.keymapper.domain.mappings.keymap.ConfigKeymapUseCaseImpl
import io.github.sds100.keymapper.domain.actions.GetActionErrorUseCaseImpl
import io.github.sds100.keymapper.domain.usecases.OnboardingUseCaseImpl
import io.github.sds100.keymapper.domain.devices.ShowDeviceInfoUseCaseImpl
import io.github.sds100.keymapper.domain.actions.TestActionUseCaseImpl

/**
 * Created by sds100 on 03/03/2021.
 */
object UseCases {
    fun configKeymap(ctx: Context) = ConfigKeymapUseCaseImpl(
        ServiceLocator.keymapRepository(ctx),
        ServiceLocator.preferenceRepository(ctx)
    )

    fun getActionError(ctx: Context) = GetActionErrorUseCaseImpl(
        ServiceLocator.preferenceRepository(ctx),
        ServiceLocator.deviceInfoRepository(ctx)
    )

    fun testAction(ctx: Context) = TestActionUseCaseImpl()

    fun onboarding(ctx: Context) = OnboardingUseCaseImpl(ServiceLocator.preferenceRepository(ctx))

    fun showDeviceInfo(ctx: Context) = ShowDeviceInfoUseCaseImpl(
        ServiceLocator.deviceInfoRepository(ctx),
        ServiceLocator.preferenceRepository(ctx)
    )
}