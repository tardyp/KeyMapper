package io.github.sds100.keymapper.ui.mappings.common

import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.domain.actions.Action
import io.github.sds100.keymapper.domain.actions.GetActionErrorUseCase
import io.github.sds100.keymapper.domain.constraints.Constraint
import io.github.sds100.keymapper.domain.constraints.ConstraintMode
import io.github.sds100.keymapper.domain.constraints.GetConstraintErrorUseCase
import io.github.sds100.keymapper.domain.models.Defaultable
import io.github.sds100.keymapper.framework.adapters.ResourceProvider
import io.github.sds100.keymapper.ui.ChipUi
import io.github.sds100.keymapper.ui.IconInfo
import io.github.sds100.keymapper.ui.actions.ActionUiHelper
import io.github.sds100.keymapper.ui.constraints.ConstraintUiHelper
import io.github.sds100.keymapper.util.result.*

/**
 * Created by sds100 on 18/03/2021.
 */
abstract class BaseMappingListItemCreator<A : Action>(
    private val getActionError: GetActionErrorUseCase,
    private val actionUiHelper: ActionUiHelper<A>,
    private val getConstraintError: GetConstraintErrorUseCase,
    private val constraintUiHelper: ConstraintUiHelper,
    private val resourceProvider: ResourceProvider
) : ResourceProvider by resourceProvider {

    fun getChipList(
        actionList: List<A>,
        constraintList: Set<Constraint>,
        constraintMode: ConstraintMode
    ): List<ChipUi> = sequence {
        val midDot = getString(R.string.middot)

        actionList.forEach { action ->
            var title: String? = null
            var icon: IconInfo? = null

            val error: Error? = actionUiHelper.getTitle(action.data)
                .onSuccess {
                    if (action.multiplier.isAllowed && action.multiplier.value is Defaultable.Custom) {
                        val multiplier = (action.multiplier.value as Defaultable.Custom<Int>).data
                        title = "${multiplier}x $it"
                    } else {
                        title = it
                    }
                }
                .then { actionUiHelper.getIcon(action.data) }.onSuccess { icon = it }
                .errorOrNull() ?: getActionError.getError(action.data)

            when {
                title != null && error == null -> {
                    val chipText = buildString {

                        append(title)

                        actionUiHelper.getOptionLabels(action).forEachIndexed { index, label ->
                            append(" $midDot ")

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
                    }

                    yield(
                        ChipUi.Normal(
                            id = action.uid,
                            text = chipText,
                            icon = icon
                        )
                    )
                }

                error != null -> yield(
                    ChipUi.Error(action.uid, error.getFullMessage(this@BaseMappingListItemCreator))
                )
            }
        }

        if (actionList.isNotEmpty() && constraintList.isNotEmpty()) {
            yield(ChipUi.Transparent("while", text = getString(R.string.chip_while)))
        }

        val constraintSeparatorText = when (constraintMode) {
            ConstraintMode.AND -> getString(R.string.constraint_mode_and)
            ConstraintMode.OR -> getString(R.string.constraint_mode_or)
        }

        constraintList.forEachIndexed { index, constraint ->
            if (index != 0) {
                yield(
                    ChipUi.Transparent(
                        id = constraintSeparatorText,
                        text = constraintSeparatorText
                    )
                )
            }

            val text: String = constraintUiHelper.getTitle(constraint)
            var icon: IconInfo? = null

            val error: Error? = constraintUiHelper.getIcon(constraint)
                .onSuccess { icon = it }
                .errorOrNull() ?: getConstraintError.invoke(constraint)

            val chip: ChipUi = if (error == null) {
                ChipUi.Normal(
                    id = constraint.toString(),
                    text = text,
                    icon = icon
                )
            } else {
                ChipUi.Error(
                    constraint.toString(),
                    error.getFullMessage(this@BaseMappingListItemCreator)
                )
            }

            yield(chip)
        }

    }.toList()
}