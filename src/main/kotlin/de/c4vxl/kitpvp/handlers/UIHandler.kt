package de.c4vxl.kitpvp.handlers

import de.c4vxl.gamelobby.lobby.Lobby.isInLobby
import de.c4vxl.kitpvp.Main
import de.c4vxl.kitpvp.ui.type.UI
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.*

/**
 * Handles different ui events
 */
class UIHandler : Listener {
    companion object {
        val nonClosable = mutableMapOf<UUID, UI>()
        val nonCancelled = mutableListOf<UUID>()

        /**
         * Marks an item as unable to be moved bypassing UIHandler.nonCancelled
         */
        fun ItemStack.immovable(): ItemStack {
            this.itemMeta = this.itemMeta.apply {
                persistentDataContainer.set(NamespacedKey.minecraft("kitpvp_immovable"), PersistentDataType.BOOLEAN, true)
            }
            return this
        }
    }
    
    init {
        Bukkit.getPluginManager().registerEvents(this, Main.instance)
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        nonClosable.remove(event.player.uniqueId)
    }

    @EventHandler
    fun onInvClose(event: InventoryCloseEvent) {
        if (!(event.player as? Player ?: return).isInLobby)
            return

        nonCancelled.remove(event.player.uniqueId)

        val ui = nonClosable[event.player.uniqueId] ?: return
        if (event.reason == InventoryCloseEvent.Reason.PLAYER) {
            Bukkit.getScheduler().runTask(Main.instance, Runnable {
                ui.open()
            })
        }

        nonClosable.remove(event.player.uniqueId)
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onInvClick(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        if (!nonCancelled.contains(player.uniqueId))
            return

        // Not on player inventories
        if (listOf(InventoryType.PLAYER, InventoryType.CRAFTING).contains(event.clickedInventory?.type))
            return

        // Don't allow these actions
        if (listOf(
            InventoryAction.DROP_ALL_SLOT, InventoryAction.DROP_ONE_SLOT, InventoryAction.DROP_ALL_CURSOR, InventoryAction.DROP_ONE_CURSOR,
            InventoryAction.HOTBAR_SWAP, InventoryAction.MOVE_TO_OTHER_INVENTORY
        ).contains(event.action)) return

        // Check for "immovable"
        fun check(item: ItemStack?): Boolean {
            val meta = item?.takeIf { it.hasItemMeta() }?.itemMeta ?: return false
            return meta.persistentDataContainer.get(NamespacedKey.minecraft("kitpvp_immovable"), PersistentDataType.BOOLEAN) == true
        }
        if (check(event.currentItem) || check(event.cursor))
            return

        event.isCancelled = false
    }
}