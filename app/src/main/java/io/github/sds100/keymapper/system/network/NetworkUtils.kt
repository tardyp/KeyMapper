package io.github.sds100.keymapper.system.network

import android.content.Context
import android.net.wifi.WifiManager
import android.os.Build
import android.telephony.TelephonyManager
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import io.github.sds100.keymapper.system.root.RootUtils
import io.github.sds100.keymapper.util.Error
import io.github.sds100.keymapper.util.Result
import io.github.sds100.keymapper.util.Success
import kotlinx.coroutines.suspendCancellableCoroutine
import splitties.systemservices.wifiManager
import java.io.File
import javax.net.ssl.SSLHandshakeException
import kotlin.coroutines.resume

/**
 * Created by sds100 on 04/04/2020.
 */
object NetworkUtils {

    suspend fun downloadFile(
        ctx: Context,
        url: String,
        filePath: String
    ): Result<File> = suspendCancellableCoroutine {

        val queue = Volley.newRequestQueue(ctx)

        val request = StringRequest(Request.Method.GET, url,
            { response ->
                val file = File(filePath)
                file.writeText(response)

                it.resume(Success(file))
            },

            { error ->
                if (error.cause is SSLHandshakeException) {
                    it.resume(Error.SSLHandshakeError)
                } else {
                    it.resume(Error.DownloadFailed)
                }
            })

        it.invokeOnCancellation {
            request.cancel()
        }

        queue.add(request)
    }

    //WiFi stuff
    fun enableWifiPreQ(ctx: Context) {
        val wifiManager = ctx.applicationContext
            .getSystemService(Context.WIFI_SERVICE) as WifiManager

        wifiManager.isWifiEnabled = true
    }

    fun disableWifiPreQ(ctx: Context) {
        val wifiManager = ctx.applicationContext
            .getSystemService(Context.WIFI_SERVICE) as WifiManager

        wifiManager.isWifiEnabled = false
    }

    fun toggleWifiPreQ(ctx: Context) {
        val wifiManager = ctx.applicationContext
            .getSystemService(Context.WIFI_SERVICE) as WifiManager

        wifiManager.isWifiEnabled = !wifiManager.isWifiEnabled
    }

    /**
     * REQUIRES ROOT!!
     */
    fun enableWifiRoot() {
        RootUtils.executeRootCommand("svc wifi enable")
    }

    /**
     * REQUIRES ROOT!!!
     */
    fun disableWifiRoot() {
        RootUtils.executeRootCommand("svc wifi disable")
    }

    /**
     * REQUIRES ROOT!!!
     */
    fun toggleWifiRoot() {
        if (isWifiEnabled()) {
            disableWifiRoot()
        } else {
            enableWifiRoot()
        }
    }
    //Mobile data stuff

    /**
     * REQUIRES ROOT!!
     */
    fun enableMobileData() {
        RootUtils.executeRootCommand("svc data enable")
    }

    /**
     * REQUIRES ROOT!!!
     */
    fun disableMobileData() {
        RootUtils.executeRootCommand("svc data disable")
    }

    /**
     * REQUIRES ROOT!!!
     */
    fun toggleMobileData(ctx: Context) {
        if (isMobileDataEnabled(ctx)) {
            disableMobileData()
        } else {
            enableMobileData()
        }
    }

    private fun isMobileDataEnabled(ctx: Context): Boolean {
        val telephonyManager = ctx.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return telephonyManager.isDataEnabled
        } else if (telephonyManager.dataState == TelephonyManager.DATA_CONNECTED) {
            return true
        }

        return false
    }

    private fun isWifiEnabled(): Boolean = wifiManager?.isWifiEnabled ?: false
}