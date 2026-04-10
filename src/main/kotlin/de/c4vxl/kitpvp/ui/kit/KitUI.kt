package de.c4vxl.kitpvp.ui.kit

import de.c4vxl.gamemanager.language.Language
import de.c4vxl.gamemanager.language.Language.Companion.language
import de.c4vxl.gamemanager.utils.ItemBuilder
import de.c4vxl.kitpvp.data.Database
import de.c4vxl.kitpvp.data.PlayerKitData
import de.c4vxl.kitpvp.data.ServerKits
import de.c4vxl.kitpvp.data.struct.kit.Kit
import de.c4vxl.kitpvp.data.struct.kit.ServerKit
import de.c4vxl.kitpvp.ui.inspect.KitInspector
import de.c4vxl.kitpvp.ui.type.UI
import de.c4vxl.kitpvp.utils.Item
import de.c4vxl.kitpvp.utils.Item.addMarginItems
import de.c4vxl.kitpvp.utils.Item.guiItem
import net.kyori.adventure.text.TextComponent
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory

class KitUI(
    val player: Player,
    val onChoose: (Kit) -> Unit,
    private val allowEdits: Boolean = true,
    val language: Language = player.language.child("kitpvp")
): UI {
    private val baseInventory: Inventory
        get() =
        Bukkit.createInventory(null, 9 * 6, language.getCmp("ui.kits.title"))
            .apply {
                // Sides
                addMarginItems(0..45 step 9, 8..53 step 9)

                // Server kits
                addMarginItems(0..8, material = Material.PINK_STAINED_GLASS_PANE, name = language.get("ui.kits.item.general_kits.name"))

                // Add server kit items
                ServerKits.kits.let { kits ->
                    repeat(14) { i ->
                        addItem(kits.getOrNull(i)?.let { serverKitItem(it, i) }
                            ?: Item.marginItem(Material.GRAY_STAINED_GLASS_PANE))
                    }
                }

                // Custom kits
                addMarginItems(27..35, material = Material.LIGHT_BLUE_STAINED_GLASS_PANE, name = language.get("ui.kits.item.your_kits.name"))

                // Add custom kit items
                PlayerKitData.getKits(player, allowEdits).let { kits ->
                    repeat(14) { i ->
                        addItem(kits.getOrNull(i)?.let { customKitItem(it, i) }
                            ?: Item.marginItem(Material.GRAY_STAINED_GLASS_PANE))
                    }

                    if (kits.isEmpty() && !allowEdits)
                        setItem(37, ItemBuilder(
                            Material.RED_STAINED_GLASS_PANE,
                            language.getCmp("ui.kits.item.notice.no_kits.0"),
                            lore = mutableListOf(
                                language.getCmp("ui.kits.item.notice.no_kits.1") as TextComponent,
                                language.getCmp("ui.kits.item.notice.no_kits.2") as TextComponent
                            )
                        ).guiItem().build())
                }
            }

    private fun serverKitItem(kit: ServerKit, idx: Int) =
        ItemBuilder(
            kit.iconMaterial,
            language.getCmp("ui.kits.item.kit.name", kit.kit.metadata.name),
            lore = buildList {
                add(language.getCmp("ui.kits.item.kit.lore.4") as TextComponent)
                if (allowEdits) add(language.getCmp("ui.kits.item.kit.lore.5") as TextComponent)
            }.toMutableList()
        )
            .guiItem {
                if (it.isRightClick) {
                    if (!allowEdits) return@guiItem

                    KitLayout(
                        player,
                        kit.kit,
                        {
                            // TODO: Save kit preference
                        }, this
                    )
                }
            }
            .build()

    private fun customKitItem(kit: Kit, idx: Int) =
        ItemBuilder(
            if (kit.isEmpty) Material.WRITABLE_BOOK
            else Material.BOOK,
            language.getCmp("ui.kits.item.kit.name", kit.metadata.name),
            lore = buildList {
                val start = if (kit.isEmpty) 4 else 0
                for (i in start..4) {
                    if (!allowEdits && i == 4)
                        continue

                    add(language.getCmp("ui.kits.item.kit.lore.${i + 1}", kit.metadata.createdAt, kit.metadata.lastEdit) as TextComponent)
                }
            }.toMutableList()
        )
            .guiItem {
                if (it.isRightClick) {
                    if (!allowEdits) return@guiItem
                    KitInspector(player, kit, onUpdate = { updated ->
                        Database.update(player) {
                            if (updated == null) {
                                if (kits.size > idx)
                                    kits.removeAt(idx)
                            } else {
                                if (kits.size <= idx)
                                    kits.add(kit)
                                else
                                    kits[idx] = updated
                            }
                        }

                        open()
                    }, returnTo = this@KitUI)
                } else if (it.isLeftClick && !kit.isEmpty) {
                    onChoose(kit)
                }
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