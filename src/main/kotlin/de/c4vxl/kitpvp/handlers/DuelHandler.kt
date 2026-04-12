package de.c4vxl.kitpvp.handlers

import de.c4vxl.gamemanager.gma.event.player.GamePlayerQuitEvent
import de.c4vxl.kitpvp.Main
import de.c4vxl.kitpvp.data.extensions.Extensions.kitData
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

/**
 * Handles duel games
 */
class DuelHandler : Listener {
    init {
        Bukkit.getPluginManager().registerEvents(this, Main.instance)
    }

    @EventHandler
    fun onDuelQuit(event: GamePlayerQuitEvent) {
        // Not a duel
        if (!event.game.kitData.isDuel)
            return

        // Send message
        event.game.players.forEach { it.bukkitPlayer.sendMessage(it.language.child("kitpvp").getCmp("msg.duel.other_left")) }

        // End game
        event.game.stop()
    }
}