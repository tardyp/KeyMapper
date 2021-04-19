package io.github.sds100.keymapper.ui.mappings.common

import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.domain.actions.Action
import io.github.sds100.keymapper.domain.constraints.ConstraintMode
import io.github.sds100.keymapper.framework.adapters.ResourceProvider
import io.github.sds100.keymapper.mappings.DisplaySimpleMappingUseCase
import io.github.sds100.keymapper.mappings.Mapping
import io.github.sds100.keymapper.mappings.isDelayBeforeNextActionAllowed
import io.github.sds100.keymapper.ui.ChipUi
import io.github.sds100.keymapper.ui.IconInfo
import io.github.sds100.keymapper.ui.actions.ActionUiHelper
import io.github.sds100.keymapper.ui.constraints.ConstraintUiHelper
import io.github.sds100.keymapper.util.result.*

/**
 * Created by sds100 on 18/03/2021.
 */
abstract class BaseMappingListItemCreator<M : Mapping<A>, A : Action>(
    private val displayMapping: DisplaySimpleMappingUseCase,
    private val actionUiHelper: ActionUiHelper<M, A>,
    private val resourceProvider: ResourceProvider
) : ResourceProvider by resourceProvider {

    private val constraintUiHelper = ConstraintUiHelper(displayMapping, resourceProvider)

    fun getActionChipList(mapping: M): List<ChipUi> = sequence {
        val midDot = getString(R.string.middot)

        mapping.actionList.forEach { action ->
            val actionTitle: String = if (action.multiplier != null) {
                "${action.multiplier}x ${actionUiHelper.getTitle(action.data)}"
            } else {
                actionUiHelper.getTitle(action.data)
            }

            val chipText = buildString {

                append(actionTitle)

                actionUiHelper.getOptionLabels(mapping, action).forEach{ label ->
                    append(" $midDot ")

                    append(label)
                }

                if (mapping.isDelayBeforeNextActionAllowed() && action.delayBeforeNextAction != null) {
                    if (this@buildString.isNotBlank()) {
                        append(" $midDot ")
                    }

                    append(
                        getString(
                            R.string.action_title_wait,
                            action.delayBeforeNextAction!!
                        )
                    )
                }
            }

            val icon: IconInfo? = actionUiHelper.getIcon(action.data)

            val error: Error? = displayMapping.getError(action.data)

            if (error == null) {
                val chip = ChipUi.Normal(id = action.uid, text = chipText, icon = icon)
                yield(chip)
            } else {
                val chip = if (error is FixableError) {
                    ChipUi.FixableError(
                        action.uid,
                        chipText,
                        error
                    )
                } else {
                    ChipUi.Error(
                        action.uid,
                        chipText
                    )
                }

                yield(chip)
            }
        }

    }.toList()

    fun getConstraintChipList(mapping: M): List<ChipUi> = sequence {
        val constraintSeparatorText = when (mapping.constraintState.mode) {
            ConstraintMode.AND -> getString(R.string.constraint_mode_and)
            ConstraintMode.OR -> getString(R.string.constraint_mode_or)
        }

        mapping.constraintState.constraints.forEachIndexed { index, constraint ->
            if (index != 0) {
                yield(
                    ChipUi.Transparent(
                        id = constraintSeparatorText,
                        text = constraintSeparatorText
                    )
                )
            }

            val text: String = constraintUiHelper.getTitle(constraint)
            val icon: IconInfo? = constraintUiHelper.getIcon(constraint)
            val error: Error? = displayMapping.getConstraintError(constraint)

            val chip: ChipUi = if (error == null) {
                ChipUi.Normal(
                    id = constraint.uid,
                    text = text,
                    icon = icon
                )
            } else {
                if (error is FixableError) {
                    ChipUi.FixableError(
                        constraint.uid,
                        text,
                        error
                    )
                } else {
                    ChipUi.Error(
                        constraint.uid,
                        text
                    )
                }
            }

            yield(chip)
        }
    }.toList()

    fun createExtraInfoString(
        mapping: M,
        actionChipList: List<ChipUi>,
        constraintChipList: List<ChipUi>
    ) = buildString {
        val midDot by lazy { getString(R.string.middot) }

        if (!mapping.isEnabled) {
            append(getString(R.string.disabled))
        }

        if (actionChipList.any { it is ChipUi.FixableError }) {
            if (this.isNotEmpty()) {
                append(" $midDot ")
            }

            append(getString(R.string.tap_actions_to_fix))
        }

        if (constraintChipList.any { it is ChipUi.FixableError }) {
            if (this.isNotEmpty()) {
                append(" $midDot ")
            }

            append(getString(R.string.tap_constraints_to_fix))
        }

        if (actionChipList.isEmpty()) {
            if (this.isNotEmpty()) {
                append(" $midDot ")
            }

            append(getString(R.string.no_actions))
        }
    }
}