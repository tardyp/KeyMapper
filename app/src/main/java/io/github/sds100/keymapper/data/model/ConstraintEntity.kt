package io.github.sds100.keymapper.data.model

import android.os.Parcelable
import androidx.annotation.IntDef
import androidx.annotation.StringDef
import com.github.salomonbrys.kotson.byArray
import com.github.salomonbrys.kotson.byString
import com.github.salomonbrys.kotson.jsonDeserializer
import com.google.gson.annotations.SerializedName
import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.util.result.Error
import io.github.sds100.keymapper.util.result.Result
import io.github.sds100.keymapper.util.result.Success
import kotlinx.android.parcel.Parcelize

/**
 * Created by sds100 on 17/03/2020.
 */
@StringDef(value = [
    ConstraintEntity.APP_FOREGROUND,
    ConstraintEntity.APP_NOT_FOREGROUND,
    ConstraintEntity.BT_DEVICE_CONNECTED,
    ConstraintEntity.BT_DEVICE_DISCONNECTED,
    ConstraintEntity.SCREEN_ON,
    ConstraintEntity.SCREEN_OFF,
    ConstraintEntity.ORIENTATION_PORTRAIT,
    ConstraintEntity.ORIENTATION_LANDSCAPE,
    ConstraintEntity.ORIENTATION_0,
    ConstraintEntity.ORIENTATION_90,
    ConstraintEntity.ORIENTATION_180,
    ConstraintEntity.ORIENTATION_270
])
annotation class ConstraintType

@IntDef(value = [
    ConstraintEntity.MODE_AND,
    ConstraintEntity.MODE_OR
])
annotation class ConstraintMode

@IntDef(value = [
    ConstraintEntity.CATEGORY_APP,
    ConstraintEntity.CATEGORY_BLUETOOTH,
    ConstraintEntity.CATEGORY_SCREEN]
)
annotation class ConstraintCategory

@Parcelize
data class ConstraintEntity(@ConstraintType
                      @SerializedName(NAME_TYPE)
                      val type: String,

                            @SerializedName(NAME_EXTRAS)
                      val extras: List<Extra>) : Parcelable {

    constructor(type: String, vararg extra: Extra) : this(type, extra.toList())

    companion object {
        //DON'T CHANGE THESE. Used for JSON serialization and parsing.
        const val NAME_TYPE = "type"
        const val NAME_EXTRAS = "extras"

        const val MODE_OR = 0
        const val MODE_AND = 1
        const val DEFAULT_MODE = MODE_AND

        //types
        const val APP_FOREGROUND = "constraint_app_foreground"
        const val APP_NOT_FOREGROUND = "constraint_app_not_foreground"
        const val APP_PLAYING_MEDIA = "constraint_app_playing_media"

        const val BT_DEVICE_CONNECTED = "constraint_bt_device_connected"
        const val BT_DEVICE_DISCONNECTED = "constraint_bt_device_disconnected"

        const val SCREEN_ON = "constraint_screen_on"
        const val SCREEN_OFF = "constraint_screen_off"

        const val ORIENTATION_0 = "constraint_orientation_0"
        const val ORIENTATION_90 = "constraint_orientation_90"
        const val ORIENTATION_180 = "constraint_orientation_180"
        const val ORIENTATION_270 = "constraint_orientation_270"
        const val ORIENTATION_PORTRAIT = "constraint_orientation_portrait"
        const val ORIENTATION_LANDSCAPE = "constraint_orientation_landscape"
        //types

        /**
         * Constraints supported by all types of mappings.
         */
        //TODO move to sealed Constraint model in domain
        val COMMON_SUPPORTED_CONSTRAINTS = listOf(
            APP_FOREGROUND,
            APP_NOT_FOREGROUND,
            BT_DEVICE_CONNECTED,
            BT_DEVICE_DISCONNECTED,
            ORIENTATION_PORTRAIT,
            ORIENTATION_LANDSCAPE,
            ORIENTATION_0,
            ORIENTATION_90,
            ORIENTATION_180,
            ORIENTATION_270
        )

        val ORIENTATION_CONSTRAINTS = arrayOf(
            ORIENTATION_PORTRAIT,
            ORIENTATION_LANDSCAPE,
            ORIENTATION_0,
            ORIENTATION_90,
            ORIENTATION_180,
            ORIENTATION_270
        )

        const val EXTRA_PACKAGE_NAME = "extra_package_name"
        const val EXTRA_BT_ADDRESS = "extra_bluetooth_device_address"
        const val EXTRA_BT_NAME = "extra_bluetooth_device_name"

        //Categories
        const val CATEGORY_APP = 0
        const val CATEGORY_BLUETOOTH = 1
        const val CATEGORY_SCREEN = 2
        const val CATEGORY_ORIENTATION = 3

        val CATEGORY_LABEL_MAP = mapOf(
            CATEGORY_APP to R.string.constraint_category_app,
            CATEGORY_BLUETOOTH to R.string.constraint_category_bluetooth,
            CATEGORY_SCREEN to R.string.constraint_category_screen,
            CATEGORY_ORIENTATION to R.string.constraint_category_orientation
        )

        fun appConstraint(@ConstraintType type: String, packageName: String): ConstraintEntity {
            return ConstraintEntity(type, Extra(EXTRA_PACKAGE_NAME, packageName))
        }

        fun btConstraint(@ConstraintType type: String, address: String, name: String): ConstraintEntity {
            return ConstraintEntity(type, Extra(EXTRA_BT_ADDRESS, address), Extra(EXTRA_BT_NAME, name))
        }

        val DESERIALIZER = jsonDeserializer {
            val type by it.json.byString(NAME_TYPE)

            val extrasJsonArray by it.json.byArray(NAME_EXTRAS)
            val extraList = it.context.deserialize<List<Extra>>(extrasJsonArray) ?: listOf()

            ConstraintEntity(type, extraList)
        }
    }

    fun getExtraData(extraId: String): Result<String> {
        val extra = extras.find { it.id == extraId } ?: return Error.ExtraNotFound(extraId)

        return Success(extra.data)
    }

    /**
     * A unique identifier describing this constraint
     */
    val uniqueId: String
        get() = buildString {
            append(type)
            extras.forEach {
                append("${it.id}${it.data}")
            }
        }
}

