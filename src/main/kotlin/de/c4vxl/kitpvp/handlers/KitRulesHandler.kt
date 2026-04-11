package de.c4vxl.kitpvp.handlers

import de.c4vxl.gamemanager.gma.GMA
import de.c4vxl.gamemanager.gma.game.Game
import de.c4vxl.gamemanager.gma.player.GMAPlayer.Companion.gma
import de.c4vxl.kitpvp.Main
import de.c4vxl.kitpvp.data.extensions.Extensions.kitData
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockExplodeEvent
import org.bukkit.event.entity.EntityExplodeEvent

/**
 * This handler takes care of implementing kit rules that need custom handling
 */
class KitRulesHandler : Listener {
    init {
        Bukkit.getPluginManager().registerEvents(this, Main.instance)
    }

    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        val game = event.player.gma.game ?: return
        val kit = game.kitData.kit ?: return

        // Block breaking enabled
        if (kit.rules.isAllowBlockBreaking)
            return

        event.isCancelled = true
    }

    /**
     * Tries to find a game based on its game world
     * @param world The world
     */
    private fun getGame(world: World): Game? =
        // We know that the game id is part of the map name
        // The reason we need to use .contains is that the server might set a custom world prefix
        GMA.registeredGames.find { world.name.contains(it.id.asString) }

    @EventHandler
    fun onExplosion(event: EntityExplodeEvent) {
        val game = getGame(event.entity.world) ?: return
        val kit = game.kitData.kit ?: return

        // Explosions enabled
        if (kit.rules.isExplosionDamage)
            return

        event.blockList().clear()
    }

    @EventHandler
    fun onExplosion(event: BlockExplodeEvent) {
        val game = getGame(event.block.world) ?: return
        val kit = game.kitData.kit ?: return

        // Explosions enabled
        if (kit.rules.isExplosionDamage)
            return

        event.blockList().clear()
    }
}