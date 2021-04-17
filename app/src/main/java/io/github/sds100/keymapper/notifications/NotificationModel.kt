package io.github.sds100.keymapper.notifications

import androidx.annotation.DrawableRes

/**
 * Created by sds100 on 16/04/2021.
 */
data class NotificationModel(
    val id: Int,
    val channel: String,
    val title: String,
    val text: String,
    @DrawableRes val icon: Int,
    /**
     * The id to send back to the notification id when the notification is clicked
     */
    val onClickActionId: String,
    val showOnLockscreen: Boolean,
    val onGoing: Boolean,
    val priority: Int,
    val actions: List<Action> = emptyList(),
    val autoCancel: Boolean = false
) {

    data class Action(val id: String, val text: String)
}