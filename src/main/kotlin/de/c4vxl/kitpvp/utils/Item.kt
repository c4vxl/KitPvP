package de.c4vxl.kitpvp.utils

import de.c4vxl.gamemanager.utils.ItemBuilder
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.EntityType
import org.bukkit.event.block.Action
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

/**
 * Adds useful item utilities
 */
object Item {
    /**
     * Adds enchantment glow without an actual enchantment appearing
     */
    fun ItemStack.enchantmentGlow(): ItemStack {
        this.itemMeta = this.itemMeta.apply {
            addUnsafeEnchantment(Enchantment.UNBREAKING, 1)
            addItemFlags(ItemFlag.HIDE_ENCHANTS)
        }

        return this
    }

    /**
     * Listens to right-clicks using this item
     * @param action The function to run when item is clicked
     */
    fun ItemBuilder.onRightClick(action: (PlayerInteractEvent) -> Unit): ItemBuilder {
        this.onEvent(PlayerInteractEvent::class.java, object : ItemBuilder.ItemEventHandler<PlayerInteractEvent> {
            override fun handle(event: PlayerInteractEvent) {
                // Action is not right-click
                if (!listOf(Action.RIGHT_CLICK_BLOCK, Action.RIGHT_CLICK_AIR).contains(event.action))
                    return

                // Cancel event
                event.isCancelled = true

                action.invoke(event)
            }
        })

        return this
    }

    /**
     * Listens to drop events of this item
     * @param action The function to run when item is dropped
     */
    fun ItemBuilder.onDrop(action: (PlayerDropItemEvent) -> Unit): ItemBuilder {
        this.onEvent(PlayerDropItemEvent::class.java, object : ItemBuilder.ItemEventHandler<PlayerDropItemEvent> {
            override fun handle(event: PlayerDropItemEvent) {
                action(event)
            }
        })

        return this
    }

    /**
     * Listens to baseInventory clicks
     * @param action The action to happen when the item gets clicked
     */
    fun ItemBuilder.guiItem(action: ((InventoryClickEvent) -> Unit)? = null): ItemBuilder {
        this.onEvent(InventoryClickEvent::class.java, object : ItemBuilder.ItemEventHandler<InventoryClickEvent> {
            override fun handle(event: InventoryClickEvent) {
                if (event.whoClicked.type != EntityType.PLAYER)
                    return

                // Cancel event
                event.isCancelled = true

                action?.invoke(event)
            }
        })

        return this
    }

    /**
     * Creates a margin item for baseInventory guis
     * @param material The material
     */
    fun marginItem(material: Material): ItemStack =
        ItemBuilder(
            material,
            Component.empty()
        ).guiItem().build()
            .apply {
                this.itemMeta = this.itemMeta.apply {
                    setMaxStackSize(1)
                    addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
                }
            }

    /**
     * Adds margin items in specific slots
     * @param ranges The ranges where to put margin items
     * @param material The material of the margin item
     */
    fun Inventory.addMarginItems(vararg ranges: IntProgression, material: Material = Material.GRAY_STAINED_GLASS_PANE) {
        val marginItem = marginItem(material)
        ranges
            .forEach {
                for (i in it)
                    setItem(i, marginItem)
            }
    }
}