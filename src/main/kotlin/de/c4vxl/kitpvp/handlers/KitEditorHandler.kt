package de.c4vxl.kitpvp.handlers

import de.c4vxl.kitpvp.Main
import de.c4vxl.kitpvp.data.KitItem
import de.c4vxl.kitpvp.ui.editor.KitEditor
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.player.PlayerDropItemEvent
import java.util.*

/**
 * Overwrites some default behaviour of the lobby plugin to make the KitEditor UI work properly
 */
class KitEditorHandler : Listener {
    companion object {
        val openEditors = mutableMapOf<UUID, KitEditor>()
        val nonClosable = mutableMapOf<UUID, KitEditor>()
    }
    
    init {
        Bukkit.getPluginManager().registerEvents(this, Main.instance)
    }

    @EventHandler
    fun onInvClose(event: InventoryCloseEvent) {
        if (nonClosable.contains(event.player.uniqueId) && event.reason == InventoryCloseEvent.Reason.PLAYER) {
            Bukkit.getScheduler().runTask(Main.instance, Runnable {
                nonClosable[event.player.uniqueId]!!.open()
            })
            return
        }

        if (!openEditors.contains(event.player.uniqueId))
            return

        // Remove open editor
        openEditors.remove(event.player.uniqueId)

        // Clear items
        event.inventory.clear()
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onInv(event: InventoryClickEvent) {
        val editor = openEditors[event.whoClicked.uniqueId] ?: return

        if (event.clickedInventory?.holder == null)
            return

        // Allow placing items
        if (event.action == InventoryAction.PLACE_ALL) {
            event.isCancelled = false
            editor.kit.inventory[event.slot] = KitItem.fromItem(event.cursor)!!

            Bukkit.getScheduler().callSyncMethod(Main.instance) {
                editor.loadLower()
            }
        }

        if (event.action == InventoryAction.PICKUP_ALL) {
            editor.kit.inventory.remove(event.slot)
        }

        // Allow dropping
        if (event.action.name.contains("DROP")) {
            event.isCancelled = false
            editor.kit.inventory.remove(event.slot)
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onDrop(event: PlayerDropItemEvent) {
        val editor = openEditors[event.player.uniqueId] ?: return

        event.isCancelled = false
        event.itemDrop.remove()
    }
}