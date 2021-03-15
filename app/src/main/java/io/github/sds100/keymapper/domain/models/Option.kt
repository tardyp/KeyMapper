package io.github.sds100.keymapper.domain.models

/**
 * Created by sds100 on 03/03/2021.
 */
import kotlinx.serialization.Serializable

@Serializable
data class Option<T>(val value: T, val isAllowed: Boolean)