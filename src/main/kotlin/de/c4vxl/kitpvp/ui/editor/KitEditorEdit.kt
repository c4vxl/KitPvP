package de.c4vxl.kitpvp.ui.editor

import de.c4vxl.gamemanager.utils.ItemBuilder
import de.c4vxl.kitpvp.data.KitItem
import de.c4vxl.kitpvp.ui.general.AnvilUI
import de.c4vxl.kitpvp.utils.Item.addMarginItems
import de.c4vxl.kitpvp.utils.Item.guiItem
import net.kyori.adventure.text.TextComponent
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import kotlin.math.max
import kotlin.math.min

/**
 * The Kit editor edit item gui
 */
class KitEditorEdit(
    val editor: KitEditor,
    var item: KitItem,
    val onUpdate: (KitItem) -> Unit
) {
    private val title = editor.language.getCmp("editor.page.edit.title", editor.kit.name)

    private val baseInventory: Inventory
        get() =
            Bukkit.createInventory(null, 9 * 5, title)
                .apply {
                    addMarginItems(0..44)

                    // Save
                    setItem(0, ItemBuilder(Material.GREEN_STAINED_GLASS_PANE, editor.language.getCmp("editor.page.edit.save"))
                        .guiItem { editor.open() }
                        .build())

                    // Item display
                    setItem(13, item.builder.guiItem().build())

                    // Rename item
                    setItem(8, ItemBuilder(Material.NAME_TAG, editor.language.getCmp("editor.page.edit.item.rename.name"))
                        .guiItem {
                            AnvilUI(it.whoClicked as Player,
                                "editor.page.edit.rename.title",
                                "editor.page.edit.rename.confirm",
                                { name ->
                                    item.name = name
                                    open()
                                })
                        }
                        .build())

                    // Change amount
                    setItem(30, ItemBuilder(
                        Material.FIREWORK_STAR,
                        editor.language.getCmp("editor.page.edit.item.amount.name", item.amount.toString()),
                        lore = buildList {
                            repeat(6) { i -> add(editor.language.getCmp("editor.page.edit.item.amount.lore.${i + 1}", item.amount.toString()) as TextComponent) }
                        }.toMutableList()
                    )
                        .guiItem {
                            // Calculate change
                            val change: Int =
                                if (it.isRightClick && it.isShiftClick) -8
                                else if (it.isRightClick) -1
                                else if (it.isLeftClick && it.isShiftClick) 8
                                else if (it.isLeftClick) 1
                                else 0

                            // Update amount
                            item.amount += change
                            item.amount = min(max(item.amount, 1), 999)

                            (it.whoClicked as Player).playSound(it.whoClicked.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 3f, 1f)
                            open()
                        }
                        .build())

                    // Enchant item
                    setItem(31, ItemBuilder(Material.ENCHANTING_TABLE, editor.language.getCmp("editor.page.edit.item.enchant.name"))
                        .guiItem { KitEditorEnchant(editor, item, onUpdate) }
                        .build())

                    // Unbreakable item
                    setItem(32, ItemBuilder(Material.BEDROCK, editor.language.getCmp("editor.page.edit.item.unbreakable.name.${item.unbreakable}"))
                        .guiItem {
                            item.unbreakable = !item.unbreakable

                            (it.whoClicked as Player).playSound(it.whoClicked.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 3f, 1f)
                            open()
                        }
                        .build())
                }

    init {
        open()
    }

    private fun open() {
        onUpdate(item)

        editor.player.openInventory(baseInventory)
        editor.player.inventory.clear()
    }
}