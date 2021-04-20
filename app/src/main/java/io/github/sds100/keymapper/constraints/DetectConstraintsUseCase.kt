package io.github.sds100.keymapper.constraints

import io.github.sds100.keymapper.system.accessibility.IAccessibilityService

/**
 * Created by sds100 on 17/04/2021.
 */

class DetectConstraintsUseCaseImpl(
    private val accessibilityService: IAccessibilityService,
) : DetectConstraintsUseCase {
    override fun isSatisfied(constraints: ConstraintState): Boolean {
        //TODO("Not yet implemented")
        return true
    }
}

interface DetectConstraintsUseCase {
    fun isSatisfied(constraints: ConstraintState): Boolean
}