package de.c4vxl.kitpvp.data.struct.game

import de.c4vxl.gamemanager.gma.game.Game
import de.c4vxl.gamemanager.gma.player.GMAPlayer
import de.c4vxl.kitpvp.data.Database
import de.c4vxl.kitpvp.data.extensions.Extensions.isServerKit
import de.c4vxl.kitpvp.data.extensions.Extensions.kitData
import de.c4vxl.kitpvp.data.struct.kit.Kit
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.block.data.BlockData
import org.bukkit.entity.Player

data class GameData(
    val game: Game,
    var kit: Kit? = null,
    var roundsRemaining: Int = -1,
    var roundsWon: MutableMap<Int, Int> = mutableMapOf(),
    val offsets: MutableMap<Player, Map<Int, Int>> = mutableMapOf(),
    var challenged: GMAPlayer? = null,
    var isTryOn: Boolean = false,
    val blocksChanged: MutableMap<Location, BlockData> = mutableMapOf(),
    var queueSkipRequests: Int = 0
) {
    val isDuel: Boolean get() =
        challenged != null

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