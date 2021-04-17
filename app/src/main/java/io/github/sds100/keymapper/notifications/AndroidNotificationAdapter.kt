package io.github.sds100.keymapper.notifications

import android.app.NotificationChannel
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.broadcastreceiver.NotificationClickReceiver
import io.github.sds100.keymapper.util.color
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

/**
 * Created by sds100 on 17/04/2021.
 */
class AndroidNotificationAdapter(
    context: Context,
    private val coroutineScope: CoroutineScope
) : NotificationAdapter {

    private val ctx = context.applicationContext
    private val manager: NotificationManagerCompat = NotificationManagerCompat.from(ctx)

    override val onNotificationActionClick = MutableSharedFlow<String>()

    override fun showNotification(notification: NotificationModel) {
        val builder = NotificationCompat.Builder(ctx, notification.channel).apply {
            color = ctx.color(R.color.colorAccent)
            setContentTitle(notification.title)
            setContentText(notification.text)

            val pendingIntent = createActionPendingIntent(notification.onClickActionId)
            setContentIntent(pendingIntent)

            setAutoCancel(notification.autoCancel)
            priority = notification.priority

            if (notification.onGoing) {
                setOngoing(true)
            }

            setSmallIcon(notification.icon)

            if (!notification.showOnLockscreen) {
                setVisibility(NotificationCompat.VISIBILITY_SECRET)
            }

            notification.actions.forEach { action ->
                addAction(
                    NotificationCompat.Action(
                        0,
                        action.text,
                        createActionPendingIntent(action.id)
                    )
                )
            }
        }

        manager.notify(notification.id, builder.build())
    }

    override fun dismissNotification(notificationId: Int) {
        manager.cancel(notificationId)
    }

    override fun createChannel(channel: NotificationChannelModel) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(
                    channel.id,
                    channel.name,
                    channel.importance
                )
            )
        }
    }

    override fun deleteChannel(channelId: String) {
        manager.deleteNotificationChannel(channelId)
    }

    fun onReceiveNotificationActionIntent(intent: Intent) {
        val actionId = intent.action ?: return

        coroutineScope.launch {
            onNotificationActionClick.emit(actionId)
        }
    }

    private fun createActionPendingIntent(actionId: String): PendingIntent {
        val intent = Intent(ctx, NotificationClickReceiver::class.java).apply {
            action = actionId
        }

        return PendingIntent.getBroadcast(ctx, 0, intent, 0)
    }
}