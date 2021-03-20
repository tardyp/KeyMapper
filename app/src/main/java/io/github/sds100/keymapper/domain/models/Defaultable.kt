package io.github.sds100.keymapper.domain.models

import kotlinx.serialization.Serializable

/**
 * Created by sds100 on 28/02/2021.
 */

/**
 * Represents a value for something that can have a custom value
 * or should use a default value that is set somewhere else.
 */
@Serializable
sealed class Defaultable<T> {
    @Serializable
    data class Custom<T>(val data: T) : Defaultable<T>()

    @Serializable
    class Default<T> : Defaultable<T>()
}

fun <T> T?.createDefaultable(): Defaultable<T> = if (this == null) {
    Defaultable.Default<T>()
} else {
    Defaultable.Custom(this)
}