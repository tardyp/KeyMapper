package io.github.sds100.keymapper.domain.mappings.keymap

import io.github.sds100.keymapper.data.model.KeyMapEntity
import io.github.sds100.keymapper.domain.adapter.ExternalDeviceAdapter
import io.github.sds100.keymapper.domain.models.Constraint
import io.github.sds100.keymapper.domain.models.ConstraintMode
import java.util.*

/**
 * Created by sds100 on 03/03/2021.
 */

@Serializable
data class KeyMap(
    val dbId: Long = NEW_ID,
    val uid: String = UUID.randomUUID().toString(),
    val trigger: KeymapTrigger = KeymapTrigger(),
    val actionDataList: List<KeymapActionData> = emptyList(),
    val constraintList: List<Constraint> = emptyList(),
    val constraintMode: ConstraintMode = ConstraintMode.AND,
    val isEnabled: Boolean = true
) {

    companion object {
        const val NEW_ID = -1L
    }

    val actionList: List<KeymapAction> = actionDataList.map {
        TODO()
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
        TODO()
    }
}