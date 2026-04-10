package de.c4vxl.kitpvp.ui.editor

import de.c4vxl.gamemanager.utils.ItemBuilder
import de.c4vxl.kitpvp.data.kit.item.KitItem
import de.c4vxl.kitpvp.handlers.UIHandler
import de.c4vxl.kitpvp.ui.editor.type.KitEditorItems
import de.c4vxl.kitpvp.utils.Item
import de.c4vxl.kitpvp.utils.Item.addMarginItems
import de.c4vxl.kitpvp.utils.Item.guiItem
import net.minecraft.world.item.equipment.ArmorType
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.inventory.Inventory

/**
 * The Kit editor gui
 */
class KitEditorArmor(
    val editor: KitEditor,
    val armorType: ArmorType
) {
    private val title = editor.language.getCmp("editor.page.armor.title", editor.kit.metadata.name)

    /**
     * Holds the different pages
     */
    private val items = mapOf(
        ArmorType.HELMET to listOf(
            Material.LEATHER_HELMET,
            Material.COPPER_HELMET,
            Material.GOLDEN_HELMET,
            Material.IRON_HELMET,
            Material.DIAMOND_HELMET,
            Material.NETHERITE_HELMET,
            Material.TURTLE_HELMET
        ),

        ArmorType.CHESTPLATE to listOf(
            Material.LEATHER_CHESTPLATE,
            Material.COPPER_CHESTPLATE,
            Material.GOLDEN_CHESTPLATE,
            Material.IRON_CHESTPLATE,
            Material.DIAMOND_CHESTPLATE,
            Material.NETHERITE_CHESTPLATE,
            Material.ELYTRA
        ),

        ArmorType.LEGGINGS to listOf(
            Material.LEATHER_LEGGINGS,
            Material.COPPER_LEGGINGS,
            Material.GOLDEN_LEGGINGS,
            Material.IRON_LEGGINGS,
            Material.DIAMOND_LEGGINGS,
            Material.NETHERITE_LEGGINGS
        ),

        ArmorType.BOOTS to listOf(
            Material.LEATHER_BOOTS,
            Material.COPPER_BOOTS,
            Material.GOLDEN_BOOTS,
            Material.IRON_BOOTS,
            Material.DIAMOND_BOOTS,
            Material.NETHERITE_BOOTS
        )
    )

    private val baseInventory: Inventory
        get() =
            Bukkit.createInventory(null, 9 * 5, title)
                .apply {
                    addMarginItems(0..17, 36..44, 0..27 step 9, 17..35 step 9)

                    setItem(0, ItemBuilder(Material.RED_STAINED_GLASS_PANE, editor.language.getCmp("editor.page.armor.back"))
                        .guiItem { editor.open() }
                        .build())

                    // Empty armor slot item
                    addItem(
                        ItemBuilder(
                            Material.ARMOR_STAND,
                            editor.language.getCmp("editor.page.armor.empty")
                        ).guiItem { select(null) }.build()
                    )

                    // Add items
                    val list = items[armorType]
                    repeat(13) { i ->
                        addItem(
                            list?.getOrNull(i)
                                ?.let { KitEditorItems.item(it, editor.language)
                                    .guiItem { _ -> select(it) }
                                    .build() }
                                ?: Item.marginItem(Material.GRAY_STAINED_GLASS_PANE)
                        )
                    }
                }

    private fun select(material: Material?) {
        // Update kit
        editor.kit.setArmorPiece(armorType, material?.let { KitItem(it) })
        editor.open()
    }

    init {
        editor.player.playSound(editor.player.location, Sound.BLOCK_SCAFFOLDING_BREAK, 5f, 0.5f)
        editor.player.openInventory(baseInventory)
        editor.player.inventory.clear()
        UIHandler.nonClosable[editor.player.uniqueId] = editor
    }
}