package io.github.sds100.keymapper.ui.constraints

import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.domain.constraints.Constraint
import io.github.sds100.keymapper.domain.constraints.GetConstraintErrorUseCase
import io.github.sds100.keymapper.framework.adapters.ResourceProvider
import io.github.sds100.keymapper.ui.IconInfo
import io.github.sds100.keymapper.util.TintType
import io.github.sds100.keymapper.util.result.Error
import io.github.sds100.keymapper.util.result.errorOrNull
import io.github.sds100.keymapper.util.result.getFullMessage
import io.github.sds100.keymapper.util.result.onSuccess

/**
 * Created by sds100 on 20/03/2021.
 */
class ConstraintListItemCreator(
    private val uiHelper: ConstraintUiHelper,
    private val getError: GetConstraintErrorUseCase,
    resourceProvider: ResourceProvider
) : ResourceProvider by resourceProvider {

    fun map(constraint: Constraint): ConstraintListItem {
        val title: String = uiHelper.getTitle(constraint)
        var icon: IconInfo? = null

        val error: Error? =
            uiHelper.getIcon(constraint).onSuccess { icon = it }
                .errorOrNull() ?: getError.invoke(constraint)

        return ConstraintListItem(
            id = constraint.uid,
            tintType = icon?.tintType ?: TintType.ERROR,
            icon = icon?.drawable ?: getDrawable(R.drawable.ic_baseline_error_outline_24),
            title = title,
            errorMessage = error?.getFullMessage(this)
        )
    }
}