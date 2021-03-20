package io.github.sds100.keymapper.ui.actions

import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.domain.actions.*
import io.github.sds100.keymapper.domain.models.Defaultable
import io.github.sds100.keymapper.domain.utils.*
import io.github.sds100.keymapper.framework.adapters.ResourceProvider
import io.github.sds100.keymapper.ui.IconInfo
import io.github.sds100.keymapper.util.*
import io.github.sds100.keymapper.util.result.*

/**
 * Created by sds100 on 22/02/2021.
 */

class ActionListItemCreator<A : Action>(
    uiHelper: ActionUiHelper<A>,
    private val getError: GetActionErrorUseCase,
    resourceProvider: ResourceProvider
) : ResourceProvider by resourceProvider, ActionUiHelper<A> by uiHelper {

    fun map(
        action: A,
        actionCount: Int
    ): ActionListItemState {
        var title: String? = null
        var icon: IconInfo? = null

        val error: Error? = getTitle(action.data)
            .onSuccess {
                if (action.multiplier.isAllowed && action.multiplier.value is Defaultable.Custom) {
                    val multiplier = (action.multiplier.value as Defaultable.Custom<Int>).data
                    title = "${multiplier}x $it"
                } else {
                    title = it
                }
            }
            .then { getIcon(action.data) }.onSuccess { icon = it }
            .errorOrNull() ?: getError.getError(action.data)

        val extraInfo = buildString {
            val midDot = getString(R.string.middot)

            getOptionLabels(action).forEachIndexed { index, label ->
                if (index != 0) {
                    append(" $midDot ")
                }

                append(label)

                action.delayBeforeNextAction.apply {
                    if (isAllowed && value is Defaultable.Custom) {
                        if (this@buildString.isNotBlank()) {
                            append(" $midDot ")
                        }

                        append(getString(R.string.action_title_wait, value.data))
                    }
                }
            }

        }.takeIf { it.isNotBlank() }

        return ActionListItemState(
            id = action.uid,
            tintType = icon?.tintType ?: TintType.ERROR,
            icon = icon?.drawable ?: getDrawable(R.drawable.ic_baseline_error_outline_24),
            title = title,
            extraInfo = extraInfo,
            errorMessage = error?.getFullMessage(this),
            dragAndDrop = actionCount > 1
        )
    }
}