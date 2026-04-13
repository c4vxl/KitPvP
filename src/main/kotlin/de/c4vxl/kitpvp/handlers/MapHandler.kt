package de.c4vxl.kitpvp.handlers

import de.c4vxl.gamemanager.gma.game.Game
import de.c4vxl.gamemanager.gma.player.GMAPlayer.Companion.gma
import de.c4vxl.kitpvp.Main
import de.c4vxl.kitpvp.data.extensions.Extensions.game
import de.c4vxl.kitpvp.data.extensions.Extensions.kitData
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockExplodeEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityExplodeEvent

/**
 * Keeps track of blocks that changed during
 */
class MapHandler : Listener {
    init {
        Bukkit.getPluginManager().registerEvents(this, Main.instance)
    }

    companion object {
        /**
         * Tries to reset the map of a game
         * @param game The game to reset the map of
         */
        fun reset(game: Game) {
            if (!game.isRunning)
                return

            // Return if world is not loaded
            val world = game.worldManager.map?.world ?: return

            // Rebuild initial state of changed blocks
            game.kitData.blocksChanged.forEach { (location, initialData) ->
                location.block.setBlockData(initialData, false)
            }

            // Un-alive all entities
            world.entities.forEach {
                if (it !is Player)
                    it.remove()
            }
        }
    }

    @EventHandler
    fun onBlockPlace(event: BlockPlaceEvent) {
        val game = event.player.gma.game ?: return

        // Return if map reset isn't enabled and map breaking is allowed
        // Don't need to waste ram in that case
        val rules = game.kitData.kit?.rules ?: return
        if (!rules.isResetMap && rules.isAllowMapBreaking)
            return

        val gameWorld = game.worldManager.map?.world ?: return

        // Wrong world
        if (gameWorld.name != event.blockPlaced.world.name)
            return

        // Keep track of the change
        game.kitData.blocksChanged[event.block.location] = event.blockReplacedState.blockData
    }

    @EventHandler
    fun onBlockExplode(event: BlockExplodeEvent) {
        val game = event.block.location.world.game ?: return

        // Return if map reset isn't enabled
        // Don't need to waste ram in that case
        if (game.kitData.kit?.rules?.isResetMap != true)
            return

        // Keep track of destroyed blocks
        event.blockList().forEach {
            game.kitData.blocksChanged[it.location] = it.blockData
        }
    }

    @EventHandler
    fun onEntityExplode(event: EntityExplodeEvent) {
        val game = event.entity.world.game ?: return

        // Return if map reset isn't enabled
        // Don't need to waste ram in that case
        if (game.kitData.kit?.rules?.isResetMap != true)
            return

        // Keep track of destroyed blocks
        event.blockList().forEach {
            game.kitData.blocksChanged[it.location] = it.blockData
        }
    }

    @EventHandler
    fun onBlockDestroy(event: BlockBreakEvent) {
        val game = event.player.gma.game ?: return

        // Return if map reset isn't enabled and map breaking is allowed
        // Don't need to waste ram in that case
        val rules = game.kitData.kit?.rules ?: return
        if (!rules.isResetMap && rules.isAllowMapBreaking)
            return

        // Handle map breaking
        if (!rules.isAllowMapBreaking) {
            event.isCancelled = true
            return
        }

        // Keep track of the change
        game.kitData.blocksChanged[event.block.location] = event.block.blockData.clone()
    }
}