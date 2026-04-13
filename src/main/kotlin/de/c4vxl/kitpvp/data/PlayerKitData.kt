package de.c4vxl.kitpvp.data

import de.c4vxl.gamelobby.Main
import de.c4vxl.gamemanager.language.Language.Companion.language
import de.c4vxl.kitpvp.data.struct.kit.Kit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player

/**
 * Access point for accessing player-specific kit data
 */
object PlayerKitData {
    /**
     * Returns the maximum amount of kits per player
     */
    val numKits get() = Main.config.getInt("config.kits.num-kits", 6)

    /**
     * Returns all kits a player owns
     */
    fun getKits(player: OfflinePlayer, addExtra: Boolean) =
        buildList {
            // Add first n kits
            addAll(Database.get(player).kits.take(numKits))

            // Add one extra kit if enough space
            if (size < numKits && addExtra) {
                val language = player.player?.language?.child("kitpvp") ?: return@buildList
                add(Kit.new(language.get("kit.name.untitled"), player))
            }
        }

    /**
     * Returns the amount of kits a player has
     * @param player The player
     */
    fun getNumKits(player: Player) =
        Database.get(player).kits.size
}