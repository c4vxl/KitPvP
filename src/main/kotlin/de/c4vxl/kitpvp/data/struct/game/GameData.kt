package de.c4vxl.kitpvp.data.struct.game

import de.c4vxl.gamemanager.gma.game.Game
import de.c4vxl.gamemanager.gma.player.GMAPlayer.Companion.gma
import de.c4vxl.kitpvp.data.Database
import de.c4vxl.kitpvp.data.PlayerKitData
import de.c4vxl.kitpvp.data.extensions.Extensions.isServerKit
import de.c4vxl.kitpvp.data.extensions.Extensions.kitData
import de.c4vxl.kitpvp.data.struct.kit.Kit
import de.c4vxl.kitpvp.data.struct.kit.ServerKit
import org.bukkit.entity.Player

data class GameData(
    val game: Game,
    var kit: Kit? = null,
    val offsets: MutableMap<Player, Map<Int, Int>> = mutableMapOf()
) {
    /**
     * Fetches the player offset preferences
     * @param player The player
     */
    fun getPlayerOffsets(player: Player): Map<Int, Int> {
        if (kit == null) return emptyMap()

        val offsets = game.kitData.offsets[player]
            ?: if (kit!!.isServerKit) Database.get(player).offsets[kit!!.metadata.name] else null

        return offsets ?: emptyMap()
    }
}