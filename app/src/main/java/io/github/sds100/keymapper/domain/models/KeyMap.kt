package io.github.sds100.keymapper.domain.models

import android.os.Parcelable
import io.github.sds100.keymapper.data.model.KeyMapEntity
import io.github.sds100.keymapper.domain.actions.ActionWithOptions
import io.github.sds100.keymapper.domain.adapter.ExternalDeviceAdapter
import kotlinx.android.parcel.Parcelize
import java.util.*

/**
 * Created by sds100 on 03/03/2021.
 */

@Parcelize
data class KeyMap(
    val dbId: Long = NEW_ID,
    val uid: String = UUID.randomUUID().toString(),
    val trigger: KeymapTrigger = KeymapTrigger(),
    val actionList: List<ActionWithOptions<KeymapActionOptions>> = emptyList(),
    val constraintList: List<Constraint> = emptyList(),
    val constraintMode: ConstraintMode = ConstraintMode.AND,
    val isEnabled: Boolean = true
) : Parcelable {
    companion object {
        const val NEW_ID = -1L
    }
}

object KeyMapEntityMapper {
    fun fromEntity(entity: KeyMapEntity, deviceAdapter: ExternalDeviceAdapter): KeyMap {
        return KeyMap(
            dbId = entity.id,
            uid = entity.uid,
            trigger = KeymapTriggerEntityMapper.fromEntity(entity.trigger, deviceAdapter),
            //TODO finish
        )
    }

    fun toEntity(model: KeyMap): KeyMapEntity {

    }
}