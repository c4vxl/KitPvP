package de.c4vxl.kitpvp.handlers

import de.c4vxl.kitpvp.Main
import de.c4vxl.kitpvp.ui.editor.KitEditor
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory

/**
 * Overwrites some default behaviour of the lobby plugin to make the KitEditor UI work properly
 */
class KitEditorHandler : Listener {
    companion object {
        val openEditors = mutableMapOf<Inventory, KitEditor>()
    }
    
    init {
        Bukkit.getPluginManager().registerEvents(this, Main.instance)
    }

    @EventHandler
    fun onInvClose(event: InventoryCloseEvent) {
        if (!openEditors.contains(event.inventory))
            return

        // Remove open editor
        openEditors.remove(event.inventory)

        // Clear items
        event.inventory.clear()
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onInv(event: InventoryClickEvent) {
        if (!openEditors.contains(event.view.topInventory))
            return

        // Allow placing items
        if (event.action == InventoryAction.PLACE_ALL)
            event.isCancelled = false

        // Allow dropping
        if (event.action.name.contains("DROP"))
            event.isCancelled = false
    }
}