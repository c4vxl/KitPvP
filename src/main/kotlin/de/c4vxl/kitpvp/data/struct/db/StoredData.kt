package de.c4vxl.kitpvp.data.struct.db

import de.c4vxl.kitpvp.data.struct.kit.Kit
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import java.util.*

/**
 * Data object stored in db
 */
data class StoredData(
    val uuid: String,
    val kits: MutableList<Kit> = mutableListOf()
) {
    /**
     * Returns the player this data belongs to
     */
    val player: OfflinePlayer
        get() = Bukkit.getOfflinePlayer(UUID.fromString(uuid))
}
