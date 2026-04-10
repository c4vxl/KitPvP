package de.c4vxl.kitpvp.data.struct.kit.item

import de.c4vxl.kitpvp.utils.TimeUtils
import org.bukkit.Bukkit
import java.util.*

data class KitMetadata(
    var name: String,
    var createdBy: String,
    var createdAt: String = TimeUtils.now,
    var lastEdit: String = TimeUtils.now
) {
    val creatorPlayer get() =
        Bukkit.getOfflinePlayer(UUID.fromString(createdBy))
}
