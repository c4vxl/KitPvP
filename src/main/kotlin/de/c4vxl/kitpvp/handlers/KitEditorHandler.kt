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
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

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

    /**
     * Updates an item in a kit
     * @param item The item to update
     * @param slot The slot
     */
    private fun update(item: ItemStack?, slot: Int, ui: Inventory) {
        val kitItem = KitItem.fromItem(item)
        val kitInv = openEditors[ui]!!.kit.inventory

        if (kitItem == null)
            kitInv.remove(slot)
        else
            kitInv[slot] = kitItem
    }

    @EventHandler
    fun onInvClose(event: InventoryCloseEvent) {
        if (!openEditors.contains(event.inventory))
            return

        // Remove open editor
        openEditors.remove(event.inventory)

        // Save
        event.player.inventory.storageContents.forEachIndexed { slot, item ->
            update(item, slot, event.inventory)
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlace(event: InventoryClickEvent) {
        if (!openEditors.contains(event.view.topInventory))
            return

        // Allow placing items
        if (event.action == InventoryAction.PLACE_ALL)
            event.isCancelled = false

        // Allow dropping
        if (event.action.name.contains("DROP"))
            event.isCancelled = false

        // Update kit
        if (event.clickedInventory?.holder != null)
            update(
                event.cursor,
                event.slot,
                event.view.topInventory
            )
    }
}