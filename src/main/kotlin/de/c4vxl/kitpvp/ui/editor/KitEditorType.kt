package de.c4vxl.kitpvp.ui.editor

import de.c4vxl.gamemanager.utils.ItemBuilder
import de.c4vxl.kitpvp.data.struct.item.ItemType
import de.c4vxl.kitpvp.data.struct.kit.item.KitItem
import de.c4vxl.kitpvp.handlers.UIHandler
import de.c4vxl.kitpvp.utils.Item
import de.c4vxl.kitpvp.utils.Item.addMarginItems
import de.c4vxl.kitpvp.utils.Item.guiItem
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.inventory.Inventory

/**
 * The Kit editor item type switcher gui
 */
class KitEditorType(
    val editor: KitEditor,
    var item: KitItem,
    val onUpdate: (KitItem) -> Unit
) {
    private val title = editor.language.getCmp("editor.page.type.title", editor.kit.metadata.name)

    private val baseInventory: Inventory
        get() =
            Bukkit.createInventory(null, 9 * 5, title)
                .apply {
                    addMarginItems(0..8, 36..44, 0..27 step 9, 8..35 step 9)

                    // Save
                    setItem(0, ItemBuilder(Material.RED_STAINED_GLASS_PANE, editor.language.getCmp("editor.page.type.back"))
                        .guiItem { editor.open() }
                        .build())

                    // Add enchantment books
                    val possible = ItemType.fromMaterial(item.material)?.materials ?: emptyList()
                    repeat(21) { i ->
                        possible.getOrNull(i)
                            ?.let { addItem(ItemBuilder(
                                it,
                                Component.translatable(it.translationKey()),
                                itemMeta = item.itemMeta
                            )
                                .guiItem { _ ->
                                    item.material = it
                                    onUpdate(item)
                                }
                                .build()) }
                            ?: addItem(Item.marginItem(Material.GRAY_STAINED_GLASS_PANE))
                    }
                }

    init {
        editor.player.playSound(editor.player.location, Sound.BLOCK_SCAFFOLDING_BREAK, 5f, 0.5f)
        editor.player.openInventory(baseInventory)
        editor.player.inventory.clear()

        UIHandler.nonClosable[editor.player.uniqueId] = editor
    }
}