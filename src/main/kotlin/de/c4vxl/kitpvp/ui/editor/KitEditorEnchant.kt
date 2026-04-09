package de.c4vxl.kitpvp.ui.editor

import com.google.gson.Gson
import de.c4vxl.gamemanager.utils.ItemBuilder
import de.c4vxl.kitpvp.Main
import de.c4vxl.kitpvp.data.KitItem
import de.c4vxl.kitpvp.utils.Item
import de.c4vxl.kitpvp.utils.Item.addMarginItems
import de.c4vxl.kitpvp.utils.Item.applicableEnchantments
import de.c4vxl.kitpvp.utils.Item.guiItem
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import kotlin.math.max
import kotlin.math.min

/**
 * The Kit editor enchant item gui
 */
class KitEditorEnchant(
    val editor: KitEditor,
    var item: KitItem,
    val onUpdate: (KitItem) -> Unit
) {
    private val title = editor.language.getCmp("editor.page.enchant.title", editor.kit.name)

    /**
     * Holds a map of enchantments to their icon materials
     */
    val enchantmentIcons: Map<Enchantment, Material> get() =
        Gson().fromJson<Map<String, String>>(Main.instance.dataFolder.resolve("enchantmentIcons.json").readText(), Map::class.java)
            .mapNotNull { (key, value) ->
                (Enchantment.values().find { it.key.value().lowercase() == key.lowercase() } ?: return@mapNotNull null) to
                        (Material.entries.find { it.name.lowercase() == value.lowercase() } ?: return@mapNotNull null)
            }.toMap()

    private val baseInventory: Inventory
        get() =
            Bukkit.createInventory(null, 9 * 5, title)
                .apply {
                    addMarginItems(0..8, 36..44, 0..27 step 9, 8..35 step 9)

                    // Save
                    setItem(0, ItemBuilder(Material.GREEN_STAINED_GLASS_PANE, editor.language.getCmp("editor.page.edit.save"))
                        .guiItem { editor.open() }
                        .build())

                    // Preview item
                    setItem(8, item.builder.guiItem().build())

                    // Reset item
                    setItem(40, ItemBuilder(Material.GRINDSTONE, editor.language.getCmp("editor.page.enchant.reset"))
                        .guiItem { event ->
                            item.enchantments = mutableMapOf()
                            (event.whoClicked as Player).playSound(event.whoClicked.location, Sound.BLOCK_GRINDSTONE_USE, 3f, 1f)
                            open()
                        }
                        .build())

                    // Add enchantment books
                    val possible = item.builder.build().applicableEnchantments
                    repeat(21) { i ->
                        possible.getOrNull(i)
                            ?.let { addItem(ItemBuilder(
                                enchantmentIcons.getOrDefault(it, Material.ENCHANTED_BOOK),
                                Component.translatable(it.translationKey()),
                                lore = buildList {
                                    add(Component.empty())
                                    repeat(5) { i -> add(
                                        editor.language.getCmp("editor.page.enchant.item.lore.${i + 1}",
                                                                item.enchantmentMap.getOrDefault(it, 0).toString()) as TextComponent) }
                                }.toMutableList()
                            )
                                .guiItem { event ->
                                    // Calculate change
                                    val change: Int =
                                        if (event.isRightClick) -1
                                        else if (event.isLeftClick) 1
                                        else 0

                                    // Update amount
                                    val changed = if (event.isShiftClick) 0 else item.enchantmentMap.getOrDefault(it, 0) + change
                                    item.enchantments[it.name] = min(999, max(changed, 0))

                                    editor.player.playSound(editor.player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 5f, 1f)
                                    open()
                                }
                                .build()) }
                            ?: addItem(Item.marginItem(Material.GRAY_STAINED_GLASS_PANE))
                    }
                }

    init {
        open()
    }

    private fun open() {
        onUpdate(item)

        editor.player.playSound(editor.player.location, Sound.BLOCK_SCAFFOLDING_BREAK, 5f, 0.5f)
        editor.player.openInventory(baseInventory)
        editor.player.inventory.clear()
    }
}