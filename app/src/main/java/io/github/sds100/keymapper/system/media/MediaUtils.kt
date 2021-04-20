package io.github.sds100.keymapper.system.media

import android.content.ComponentName
import android.content.Context
import android.media.AudioManager
import android.media.session.MediaController
import android.view.KeyEvent
import io.github.sds100.keymapper.ServiceLocator
import io.github.sds100.keymapper.system.permissions.Permission
import io.github.sds100.keymapper.system.notifications.NotificationReceiver
import io.github.sds100.keymapper.util.*
import splitties.systemservices.mediaSessionManager

/**
 * Created by sds100 on 05/11/2018.
 */
object MediaUtils {
    fun playMedia(ctx: Context) = sendMediaKeyEvent(ctx, KeyEvent.KEYCODE_MEDIA_PLAY)

    fun playMediaForPackage(ctx: Context, packageName: String
    ) = sendMediaKeyEvent(ctx, KeyEvent.KEYCODE_MEDIA_PLAY, packageName)

    fun pauseMediaPlayback(ctx: Context) = sendMediaKeyEvent(ctx, KeyEvent.KEYCODE_MEDIA_PAUSE)

    fun pauseMediaForPackage(ctx: Context, packageName: String
    ) = sendMediaKeyEvent(ctx, KeyEvent.KEYCODE_MEDIA_PAUSE, packageName)

    fun playPauseMediaPlayback(ctx: Context) = sendMediaKeyEvent(ctx, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)

    fun playPauseMediaPlaybackForPackage(ctx: Context, packageName: String
    ) = sendMediaKeyEvent(ctx, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, packageName)

    fun nextTrack(ctx: Context) = sendMediaKeyEvent(ctx, KeyEvent.KEYCODE_MEDIA_NEXT)

    fun nextTrackForPackage(ctx: Context, packageName: String
    ) = sendMediaKeyEvent(ctx, KeyEvent.KEYCODE_MEDIA_NEXT, packageName)

    fun previousTrack(ctx: Context) = sendMediaKeyEvent(ctx, KeyEvent.KEYCODE_MEDIA_PREVIOUS)

    fun previousTrackForPackage(ctx: Context, packageName: String
    ) = sendMediaKeyEvent(ctx, KeyEvent.KEYCODE_MEDIA_PREVIOUS, packageName)

    fun fastForward(ctx: Context) = sendMediaKeyEvent(ctx, KeyEvent.KEYCODE_MEDIA_FAST_FORWARD)

    fun fastForwardForPackage(ctx: Context, packageName: String
    ) = sendMediaKeyEvent(ctx, KeyEvent.KEYCODE_MEDIA_FAST_FORWARD, packageName)

    fun rewind(ctx: Context) = sendMediaKeyEvent(ctx, KeyEvent.KEYCODE_MEDIA_REWIND)

    fun rewindForPackage(ctx: Context, packageName: String
    ) = sendMediaKeyEvent(ctx, KeyEvent.KEYCODE_MEDIA_REWIND, packageName)

    private fun sendMediaKeyEvent(ctx: Context, keyCode: Int) {
        //simulates media key being pressed.
        val audioManager = ctx.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, keyCode))
        audioManager.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_UP, keyCode))
    }

    private fun sendMediaKeyEvent(ctx: Context, keyCode: Int, packageName: String) {
        getActiveMediaSessions(ctx).onSuccess { mediaSessions ->
            for (session in mediaSessions) {
                if (session.packageName == packageName) {
                    session.dispatchMediaButtonEvent(KeyEvent(KeyEvent.ACTION_DOWN, keyCode))
                    session.dispatchMediaButtonEvent(KeyEvent(KeyEvent.ACTION_UP, keyCode))
                    break
                }
            }
        }
    }

    fun highestPriorityPackagePlayingMedia(ctx: Context): Result<String> {
        return getActiveMediaSessions(ctx) then { mediaSessions ->
            Success(mediaSessions.map { it.packageName })
        } then {
            val packageName = it.elementAtOrNull(0)

            if (packageName == null) {
                Error.NoMediaSessions
            } else {
                Success(packageName)
            }
        }
    }

    private fun getActiveMediaSessions(ctx: Context): Result<List<MediaController>> {
        if (ServiceLocator.permissionAdapter(ctx).isGranted(Permission.NOTIFICATION_LISTENER)) {

            val component = ComponentName(ctx, NotificationReceiver::class.java)

            return Success(mediaSessionManager.getActiveSessions(component))
        }

        return Error.PermissionDenied(Permission.NOTIFICATION_LISTENER)
    }
}