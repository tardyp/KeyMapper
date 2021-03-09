package io.github.sds100.keymapper.domain.models

/**
 * Created by sds100 on 28/02/2021.
 */

/**
 * Represents a value for something that can have a custom value
 * or should use a default value that is set somewhere else.
 */
sealed class Defaultable<T> {
    data class Custom<T>(val value: T) : Defaultable<T>()
    class Default<T> : Defaultable<T>()
}