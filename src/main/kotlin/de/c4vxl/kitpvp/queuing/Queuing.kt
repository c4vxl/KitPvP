package de.c4vxl.kitpvp.queuing

import de.c4vxl.gamemanager.gma.GMA
import de.c4vxl.gamemanager.gma.game.Game
import de.c4vxl.gamemanager.gma.game.type.GameSize
import de.c4vxl.kitpvp.data.extensions.Extensions.kitData
import de.c4vxl.kitpvp.data.struct.kit.Kit

/**
 * Handles the creation / queuing of games
 */
object Queuing {
    /**
     * Attempts to find a game of specific size
     * If no game can be found and kit is specified, a new game will be created
     *
     * @param size The size to look for
     * @param kit The kit to look for/create a game with
     */
    fun getGame(size: GameSize, kit: Kit? = null): Game? {
        // Find game of correct size and kit
        return findGame(size, kit)

            // Or create a new game with kit
            ?: kit?.let { hostGame(size, it) }
    }

    /**
     * Creates a new game with a specific kit
     * @param size The size of the game
     * @param kit The kit to use
     */
    fun hostGame(size: GameSize, kit: Kit) =
        GMA.createGame(size).apply {
            kitData.kit = kit
        }

    /**
     * Attempts to find a game of specific size and (optional) kit
     * @param size The size to look for
     * @param kit The kit to look for (if null, will be ignored)
     */
    fun findGame(size: GameSize, kit: Kit? = null): Game? =
        GMA.registeredGames.find {
            return@find it.isQueuing
                    && it.size.equals(size.teamAmount, size.teamSize)
                    && !it.isPrivate
                    && (it.kitData.kit == kit || kit == null)
        }
}