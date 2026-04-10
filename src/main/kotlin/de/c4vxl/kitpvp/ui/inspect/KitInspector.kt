package de.c4vxl.kitpvp.ui.inspect

import de.c4vxl.gamemanager.language.Language
import de.c4vxl.gamemanager.language.Language.Companion.language
import de.c4vxl.gamemanager.utils.ItemBuilder
import de.c4vxl.kitpvp.data.Kit
import de.c4vxl.kitpvp.ui.editor.KitEditor
import de.c4vxl.kitpvp.ui.editor.KitEditorRename
import de.c4vxl.kitpvp.ui.type.UI
import de.c4vxl.kitpvp.utils.Item
import de.c4vxl.kitpvp.utils.Item.addMarginItems
import de.c4vxl.kitpvp.utils.Item.guiItem
import de.c4vxl.kitpvp.utils.TryOn
import net.kyori.adventure.text.TextComponent
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory

class KitInspector(
    val player: Player,
    var kit: Kit,
    val language: Language = player.language.child("kitpvp"),
    val onUpdate: (Kit?) -> Unit
) : UI {
    private val baseInventory: Inventory get() =
        Bukkit.createInventory(null, 9 * 5, language.getCmp("inspector.title", kit.metadata.name))
            .apply {
                addMarginItems(0..17, 27..44, 18..18, 26..26)
                addMarginItems(11..15, 29..33, 10..28 step 9, 16..34 step 9, material = Material.LIME_STAINED_GLASS_PANE)

                setItem(8, ItemBuilder(
                    Material.ITEM_FRAME,
                    language.getCmp("inspector.item.about.name"),
                    lore = buildList {
                        repeat(2) { i ->
                            add(language.getCmp(
                                "inspector.item.about.lore.${i + 1}",
                                kit.metadata.createdAt,
                                kit.metadata.creatorPlayer.name ?: "/"
                            ) as TextComponent)
                        }
                    }.toMutableList()
                )
                    .build())

                addItem(item(Material.NAME_TAG, "rename") {
                    KitEditorRename.open(player, kit, {
                        onUpdate(it)
                        kit = it
                        open()
                    }, this@KitInspector)
                })

                addItem(item(Material.ANVIL, "edit") {
                    KitEditor(player, kit, onDone = {
                        onUpdate(it)
                        kit = it
                    }, onClose = {
                        open()
                    })
                })

                addItem(item(Material.NETHER_STAR, "tryon") { TryOn.open(player, kit) })

                addItem(Item.marginItem(Material.GRAY_STAINED_GLASS_PANE))

                addItem(item(Material.BARRIER, "delete") {
                    if (it.isShiftClick)
                        onUpdate(null)
                })

                addItem(ItemBuilder(Material.BARRIER, language.getCmp("inspector.item.delete"))
                    .guiItem {
                        player.closeInventory()
                        onUpdate(null)
                    }
                    .build())
            }

    private fun item(material: Material, key: String, onClick: (InventoryClickEvent) -> Unit) =
        ItemBuilder(
            material,
            language.getCmp("inspector.item.$key.name"),
            lore = mutableListOf(
                language.getCmp("inspector.item.$key.desc") as TextComponent
            )
        )
            .guiItem {
                onClick(it)
                player.playSound(player.location, Sound.BLOCK_SCAFFOLDING_BREAK, 5f, 0.5f)
            }
            .build()

    init {
        open()
    }

    override fun open() {
        player.playSound(player.location, Sound.BLOCK_SCAFFOLDING_BREAK, 5f, 0.5f)
        player.openInventory(baseInventory)
        player.inventory.clear()
    }
}