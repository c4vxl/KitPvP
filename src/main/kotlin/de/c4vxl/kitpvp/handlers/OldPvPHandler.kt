package de.c4vxl.kitpvp.handlers

import de.c4vxl.gamemanager.gma.event.game.GameEndEvent
import de.c4vxl.gamemanager.gma.event.player.GamePlayerEquipEvent
import de.c4vxl.gamemanager.gma.event.player.GamePlayerQuitEvent
import de.c4vxl.gamemanager.gma.player.GMAPlayer.Companion.gma
import de.c4vxl.kitpvp.Main
import de.c4vxl.kitpvp.data.extensions.Extensions.kitData
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.inventory.EquipmentSlot

/**
 * Tries to recreate the old pvp system experience
 */
class OldPvPHandler : Listener {
    companion object {
        /**
         * Holds a list of disabled items
         */
        val disabledItems = mutableListOf(
            Material.SHIELD
        )
    }

    init {
        Bukkit.getPluginManager().registerEvents(this, Main.instance)
    }

    @EventHandler
    fun onEquip(event: GamePlayerEquipEvent) {
        // Old pvp is not enabled
        if (event.game.kitData.kit?.rules?.isOldPvP != true)
            return

        // Disable hit cooldown
        event.player.bukkitPlayer.getAttribute(Attribute.ATTACK_SPEED)!!.baseValue = 1024.0
    }

    @EventHandler
    fun onQuit(event: GamePlayerQuitEvent) {
        // Reset hit cooldown to default
        event.player.bukkitPlayer.getAttribute(Attribute.ATTACK_SPEED)?.let {
            it.baseValue = it.defaultValue
        }
    }

    @EventHandler
    fun onEnd(event: GameEndEvent) {
        event.game.players.forEach { player ->
            // Reset hit cooldown to default
            player.bukkitPlayer.getAttribute(Attribute.ATTACK_SPEED)?.let {
                it.baseValue = it.defaultValue
            }
        }
    }

    @EventHandler
    fun onHit(event: EntityDamageByEntityEvent) {
        val player = event.damager as? Player ?: return
        val game = player.gma.game?.takeIf { it.isRunning } ?: return
        val kit = game.kitData.kit ?: return

        val damaged = event.entity as? Player

        // Old pvp is not enabled
        if (!kit.rules.isOldPvP)
            return

        // Reset cooldown
        player.setCooldown(player.inventory.itemInMainHand, 0)

        // Cancel sweep damage
        if (event.cause == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK) {
            event.isCancelled = true
        }

        // Apply old knock-back
        damaged?.let {
            val direction = it.location.toVector().subtract(player.location.toVector()).normalize()
            try {
                it.velocity = direction.multiply(0.4).setY(0.33)
            } catch (_: Exception) {}
        }

        // Disable critical damage
        if (event.cause == EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
            if (player.fallDistance > 0.0f && !player.isOnGround)
                event.damage *= 0.75
        }
        
        // Sprint reset
        player.isSprinting = false
    }

    @EventHandler
    fun onItemUse(event: PlayerInteractEvent) {
        val item = event.item ?: return
        val game = event.player.gma.game?.takeIf { it.isRunning } ?: return
        val kit = game.kitData.kit ?: return

        // Old pvp is not enabled
        if (!kit.rules.isOldPvP)
            return

        // Not a disabled item
        if (!disabledItems.contains(item.type))
            return

        event.isCancelled = true
    }

    @EventHandler
    fun offhandSwap(event: PlayerSwapHandItemsEvent) {
        val game = event.player.gma.game?.takeIf { it.isRunning } ?: return
        val kit = game.kitData.kit ?: return

        // Old pvp is not enabled
        if (!kit.rules.isOldPvP)
            return

        event.isCancelled = true
    }

    @EventHandler
    fun offhandSwap(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        val game = player.gma.game?.takeIf { it.isRunning } ?: return
        val kit = game.kitData.kit ?: return

        // Old pvp is not enabled
        if (!kit.rules.isOldPvP)
            return

        // Not offhand
        val isOffhand = event.slotType == InventoryType.SlotType.QUICKBAR && event.slot == 40 && event.inventory.type == InventoryType.CRAFTING
        if (!isOffhand)
            return

        event.isCancelled =true
    }
}