package de.c4vxl.kitpvp.handlers

import de.c4vxl.gamemanager.gma.event.game.GameStopEvent
import de.c4vxl.gamemanager.gma.event.game.GameWorldLoadedEvent
import de.c4vxl.gamemanager.gma.event.player.GamePlayerEquipEvent
import de.c4vxl.kitpvp.Main
import de.c4vxl.kitpvp.data.extensions.Extensions.data
import de.c4vxl.kitpvp.data.extensions.Extensions.kitData
import org.bukkit.Bukkit
import org.bukkit.GameRules
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class GameHandler : Listener {
    init {
        Bukkit.getPluginManager().registerEvents(this, Main.instance)
    }

    @EventHandler
    fun onGameStop(event: GameStopEvent) {
        data.remove(event.game.id)
    }

    @EventHandler
    fun onWorldLoaded(event: GameWorldLoadedEvent) {
        val kit = event.game.kitData.kit ?: return
        val rules = kit.rules
        val world = event.map.world ?: return

        // Set world game rules
        mapOf(
            // Kit rules based game rules
            listOf(GameRules.BLOCK_DROPS, GameRules.MOB_DROPS, GameRules.ENTITY_DROPS) to rules.isItemDrop,
            listOf(GameRules.KEEP_INVENTORY) to rules.isKeepInventory,
            listOf(GameRules.ADVANCE_TIME) to rules.isAlwaysDay,
            listOf(GameRules.FALL_DAMAGE) to rules.isFallDamage,

            // Independent game rules
            listOf(GameRules.ADVANCE_WEATHER) to false
        ).forEach { (gameRules, value) -> gameRules.forEach { world.setGameRule(it, value) } }
    }

    @EventHandler
    fun onEquip(event: GamePlayerEquipEvent) {
        val kit = event.game.kitData.kit ?: return
        val player = event.player.bukkitPlayer

        // Don't re-equip the player on respawns if keep inventory is disabled in kit rules
        if (event.reason == GamePlayerEquipEvent.Reason.RESPAWN && !kit.rules.isKeepInventory)
            return

        // Equip player
        kit.equip(player, event.game.kitData.getPlayerOffsets(player))
        kit.rules.giveEffects(player)
    }
}