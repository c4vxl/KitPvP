package de.c4vxl.kitpvp.ui.editor

import de.c4vxl.gamemanager.utils.ItemBuilder
import de.c4vxl.kitpvp.data.struct.item.ItemType
import de.c4vxl.kitpvp.data.struct.kit.item.KitItem
import de.c4vxl.kitpvp.handlers.UIHandler
import de.c4vxl.kitpvp.ui.general.AnvilUI
import de.c4vxl.kitpvp.ui.general.PotionEffectsUI
import de.c4vxl.kitpvp.ui.type.UI
import de.c4vxl.kitpvp.utils.Item.addMarginItems
import de.c4vxl.kitpvp.utils.Item.applicableEnchantments
import de.c4vxl.kitpvp.utils.Item.guiItem
import de.c4vxl.kitpvp.utils.Item.marginItem
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
) : UI {
    private val title = editor.language.getCmp("editor.page.edit.title", editor.kit.metadata.name)

    private val baseInventory: Inventory
        get() =
            Bukkit.createInventory(null, 9 * 5, title)
                .apply {
                    addMarginItems(0..28, 34..44)

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
                                }, returnTo = this@KitEditorEdit)
                        }
                        .build())

                    // Change amount
                    addItem(ItemBuilder(
                        Material.FIREWORK_STAR,
                        editor.language.getCmp("editor.page.edit.item.amount.name", item.amount.toString()),
                        lore = buildList {
                            repeat(7) { i -> add(editor.language.getCmp("editor.page.edit.item.amount.lore.${i + 1}", item.amount.toString(), item.material.maxStackSize.toString()) as TextComponent) }
                        }.toMutableList()
                    )
                        .guiItem {
                            // Calculate change
                            val change: Int =
                                if (it.isRightClick && it.isShiftClick) -8
                                else if (it.isRightClick) -1
                                else if (it.isLeftClick && it.isShiftClick) 8
                                else if (it.isLeftClick) 1
                                else if (it.action.name.contains("DROP")) item.material.maxStackSize - item.amount
                                else 0

                            // Update amount
                            item.amount += change
                            item.amount = min(max(item.amount, 1), 999)

                            (it.whoClicked as Player).playSound(it.whoClicked.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 3f, 1f)
                            open()
                        }
                        .build())

                    fun filler(key: String) =
                        addItem(marginItem(Material.BLACK_STAINED_GLASS_PANE, editor.language.get("editor.page.edit.item.$key.not_available"))
                            .apply { itemMeta = itemMeta.apply { lore(listOf(editor.language.getCmp("editor.page.edit.item.not_available.lore"))) } })

                    // Enchant item
                    item.builder.build().applicableEnchantments
                        .takeIf { it.isNotEmpty() }
                        ?.let { enchantments ->
                            addItem(ItemBuilder(Material.ENCHANTING_TABLE, editor.language.getCmp("editor.page.edit.item.enchant.name"))
                                .guiItem { KitEditorEnchant(editor, item, enchantments, onUpdate) }
                                .build())
                        }
                        ?: filler("enchant")

                    // Unbreakable item
                    if (item.material.maxDurability.toInt() != 0)
                        addItem(ItemBuilder(Material.BEDROCK, editor.language.getCmp("editor.page.edit.item.unbreakable.name.${item.unbreakable}"))
                            .guiItem {
                                item.unbreakable = !item.unbreakable

                                (it.whoClicked as Player).playSound(it.whoClicked.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 3f, 1f)
                                open()
                            }
                            .build())
                    else
                        filler("unbreakable")

                    // Type changer
                    val type = ItemType.fromMaterial(item.material)
                    if (type != null)
                        addItem(ItemBuilder(Material.FLINT, editor.language.getCmp("editor.page.edit.item.type.name"))
                            .guiItem {
                                KitEditorType(editor, item) {
                                    item = it
                                    open()
                                }
                            }
                            .build())
                    else
                        filler("type")

                    // Effects changer
                    if (item.material.name.contains("ARROW") || item.material.name.contains("POTION"))
                        addItem(ItemBuilder(Material.BREWING_STAND, editor.language.getCmp("editor.page.edit.item.effect.name"))
                            .guiItem {
                                PotionEffectsUI(
                                    editor.player,
                                    "editor.page.edit.effect.title",
                                    item.effectsMap.toMutableMap(),
                                    {
                                        if (it.isNotEmpty() && type == ItemType.ARROW)
                                            item.material = Material.TIPPED_ARROW

                                        item.effects = it.mapKeys { e -> e.key.name }.toMutableMap()

                                        onUpdate(item)
                                        open()
                                    }
                                )
                            }
                            .build())
                    else
                        filler("effect")
                }

    init {
        open()
    }

    override fun open() {
        onUpdate(item)

        editor.player.playSound(editor.player.location, Sound.BLOCK_SCAFFOLDING_BREAK, 5f, 0.5f)
        editor.player.openInventory(baseInventory)
        editor.player.inventory.clear()

        UIHandler.nonClosable[editor.player.uniqueId] = editor
    }
}