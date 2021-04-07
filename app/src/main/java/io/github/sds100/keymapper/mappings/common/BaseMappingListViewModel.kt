package io.github.sds100.keymapper.mappings.common

import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.framework.adapters.ResourceProvider
import io.github.sds100.keymapper.ui.ChipUi
import io.github.sds100.keymapper.ui.UserResponseViewModel
import io.github.sds100.keymapper.ui.UserResponseViewModelImpl
import io.github.sds100.keymapper.ui.dialogs.RequestUserResponse
import io.github.sds100.keymapper.ui.getUserResponse
import io.github.sds100.keymapper.util.result.Error
import io.github.sds100.keymapper.util.result.FixableError
import io.github.sds100.keymapper.util.result.getFullMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/**
 * Created by sds100 on 07/04/2021.
 */
abstract class BaseMappingListViewModel(
    private val coroutineScope: CoroutineScope,
    resourceProvider: ResourceProvider
) :
    ResourceProvider by resourceProvider, UserResponseViewModel by UserResponseViewModelImpl() {

    private val _fixError = MutableSharedFlow<FixableError>()
    val fixError = _fixError.asSharedFlow()

    fun onActionChipClick(chipModel: ChipUi) {
        if (chipModel is ChipUi.FixableError) {
            showSnackBarAndFixError(chipModel.error)
        }
    }

    fun onConstraintsChipClick(chipModel: ChipUi) {
        if (chipModel is ChipUi.FixableError) {
            showSnackBarAndFixError(chipModel.error)
        }
    }

    private fun showSnackBarAndFixError(error: FixableError) {
        coroutineScope.launch {
            val snackBar = RequestUserResponse.SnackBar(
                title = error.getFullMessage(this@BaseMappingListViewModel),
                actionText = getString(R.string.snackbar_fix)
            )

            getUserResponse("fix_constraint", snackBar) ?: return@launch

            _fixError.emit(error)
        }
    }
}