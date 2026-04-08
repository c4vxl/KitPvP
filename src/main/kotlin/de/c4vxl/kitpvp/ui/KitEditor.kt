package de.c4vxl.kitpvp.ui

import de.c4vxl.gamemanager.language.Language
import de.c4vxl.gamemanager.language.Language.Companion.language
import de.c4vxl.gamemanager.utils.ItemBuilder
import de.c4vxl.kitpvp.utils.Item
import de.c4vxl.kitpvp.utils.Item.guiItem
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory

/**
 * The Kit editor gui
 */
class KitEditor(
    val player: Player,
    val language: Language = player.language.child("kitpvp"),
    val kitName: String
) {
    val inventory: Inventory
        get() =
            Bukkit.createInventory(null, 9 * 6, language.getCmp("editor.page.main.title", kitName))
                .apply {
                    for (i in 0..8)
                        setItem(i, Item.marginItem(Material.BLACK_STAINED_GLASS_PANE))
                    
                    val marginItem = Item.marginItem(Material.GRAY_STAINED_GLASS_PANE)
                    listOf(10..17, 46..53, 10..37 step 9, 17..45 step 9)
                        .forEach {
                            for (i in it)
                                setItem(i, marginItem)
                        }

                    // Exit items
                    setItem(0, item(Material.RED_STAINED_GLASS_PANE, "discard").guiItem { discard(it) }.build())
                    setItem(8, item(Material.GREEN_STAINED_GLASS_PANE, "save").guiItem { save(it) }.build())

                    // Armor items
                    setItem(9, item(Material.ITEM_FRAME, "offhand").guiItem {  }.build())
                    setItem(18, item(Material.ARMOR_STAND, "helmet").guiItem {  }.build())
                    setItem(27, item(Material.ARMOR_STAND, "chestplate").guiItem {  }.build())
                    setItem(36, item(Material.ARMOR_STAND, "leggings").guiItem {  }.build())
                    setItem(45, item(Material.ARMOR_STAND, "boots").guiItem {  }.build())

                    // Tab items
                    setItem(12, item(Material.DIAMOND_SWORD, "weapons", "section").guiItem {}.build())
                    setItem(13, item(Material.COOKED_BEEF, "consumables", "section").guiItem {}.build())
                    setItem(14, item(Material.END_STONE, "blocks", "section").guiItem {}.build())
                    setItem(15, item(Material.DIAMOND, "utils", "section").guiItem {}.build())
                }

    /**
     * Creates a base gui item
     * @param material The material of the item
     * @param key The name of the item in the translation key
     * @param category The section of the translation key
     */
    private fun item(material: Material, key: String, category: String = "item"): ItemBuilder {
        val lore = language.getCmp("editor.$category.$key.lore").takeIf {
            MiniMessage.miniMessage().serialize(it) != "editor.$category.$key.lore"
        }

        return ItemBuilder(
            material,
            language.getCmp("editor.$category.$key.name"),
            lore = mutableListOf(lore as? TextComponent).filterNotNull().toMutableList()
        )
    }

    /**
     * Returns a base inventory and adds a list of items
     * @param items The list of items (more than 18 will be discarded)
     */
    private fun withItems(items: List<ItemBuilder>): Inventory =
        inventory.apply {
            val marginItem = Item.marginItem(Material.BLACK_STAINED_GLASS_PANE)

            for (i in 0..17) {
                addItem(
                    items.getOrNull(i)
                        ?.build()
                        ?: marginItem
                )
            }
        }

    fun discard(event: InventoryClickEvent) {

    }

    fun save(event: InventoryClickEvent) {

    }

    fun open() =
        player.openInventory(inventory)
}