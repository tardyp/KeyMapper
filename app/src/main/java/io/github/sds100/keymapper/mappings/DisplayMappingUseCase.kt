package io.github.sds100.keymapper.mappings

import android.graphics.drawable.Drawable
import io.github.sds100.keymapper.constraints.GetConstraintErrorUseCase
import io.github.sds100.keymapper.domain.actions.GetActionErrorUseCase
import io.github.sds100.keymapper.domain.adapter.InputMethodAdapter
import io.github.sds100.keymapper.domain.adapter.PermissionAdapter
import io.github.sds100.keymapper.domain.adapter.ServiceAdapter
import io.github.sds100.keymapper.domain.ime.KeyMapperImeHelper
import io.github.sds100.keymapper.domain.packages.PackageManagerAdapter
import io.github.sds100.keymapper.util.result.FixableError
import io.github.sds100.keymapper.util.result.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge

/**
 * Created by sds100 on 03/04/2021.
 */

class DisplaySimpleMappingUseCaseImpl(
    private val packageManager: PackageManagerAdapter,
    private val permissionAdapter: PermissionAdapter,
    private val inputMethodAdapter: InputMethodAdapter,
    private val serviceAdapter: ServiceAdapter,
    getActionError: GetActionErrorUseCase,
    getConstraintError: GetConstraintErrorUseCase
) : DisplaySimpleMappingUseCase, GetActionErrorUseCase by getActionError,
    GetConstraintErrorUseCase by getConstraintError {

    private val keyMapperImeHelper = KeyMapperImeHelper(inputMethodAdapter)

    override val invalidateErrors: Flow<Unit> =
        merge(
            inputMethodAdapter.chosenIme.drop(1).map { }, //dont collect the initial value
            permissionAdapter.onPermissionsUpdate
        )

    override fun getAppName(packageName: String): Result<String> =
        packageManager.getAppName(packageName)

    override fun getAppIcon(packageName: String): Result<Drawable> =
        packageManager.getAppIcon(packageName)

    override fun getInputMethodLabel(imeId: String): Result<String> =
        inputMethodAdapter.getLabel(imeId)

    override fun fixError(error: FixableError) {
        when (error) {
            FixableError.AccessibilityServiceDisabled -> serviceAdapter.enableService()
            is FixableError.AppDisabled -> packageManager.enableApp(error.packageName)
            is FixableError.AppNotFound -> packageManager.installApp(error.packageName)
            FixableError.NoCompatibleImeChosen -> keyMapperImeHelper.chooseCompatibleInputMethod(
                fromForeground = true
            )
            FixableError.NoCompatibleImeEnabled -> keyMapperImeHelper.enableCompatibleInputMethods()
            is FixableError.PermissionDenied -> permissionAdapter.request(error.permission)
        }
    }
}

interface DisplaySimpleMappingUseCase : DisplayActionUseCase, DisplayConstraintUseCase

interface DisplayActionUseCase : GetActionErrorUseCase {
    fun getAppName(packageName: String): Result<String>
    fun getAppIcon(packageName: String): Result<Drawable>
    fun getInputMethodLabel(imeId: String): Result<String>
    fun fixError(error: FixableError)
}

interface DisplayConstraintUseCase : GetConstraintErrorUseCase {
    fun getAppName(packageName: String): Result<String>
    fun getAppIcon(packageName: String): Result<Drawable>
    fun getInputMethodLabel(imeId: String): Result<String>
    fun fixError(error: FixableError)
}