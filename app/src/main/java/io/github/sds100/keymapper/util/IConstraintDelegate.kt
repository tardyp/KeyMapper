package io.github.sds100.keymapper.util

import io.github.sds100.keymapper.data.model.ConstraintEntity

/**
 * Created by sds100 on 13/12/20.
 */
interface IConstraintDelegate {
    fun Array<ConstraintEntity>.constraintsSatisfied(mode: Int): Boolean
}