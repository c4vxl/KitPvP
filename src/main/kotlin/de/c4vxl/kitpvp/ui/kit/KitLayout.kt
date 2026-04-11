package de.c4vxl.kitpvp.ui.kit

import de.c4vxl.gamemanager.language.Language
import de.c4vxl.gamemanager.language.Language.Companion.language
import de.c4vxl.gamemanager.utils.ItemBuilder
import de.c4vxl.kitpvp.data.struct.kit.Kit
import de.c4vxl.kitpvp.data.struct.kit.item.KitItem
import de.c4vxl.kitpvp.handlers.UIHandler
import de.c4vxl.kitpvp.handlers.UIHandler.Companion.immovable
import de.c4vxl.kitpvp.ui.type.UI
import de.c4vxl.kitpvp.utils.Item.addMarginItems
import de.c4vxl.kitpvp.utils.Item.guiItem
import de.c4vxl.kitpvp.utils.KitPreferenceUtils
import net.kyori.adventure.text.TextComponent
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

class KitLayout(
    val player: Player,
    val kit: Kit,
    val onChoose: (Map<Int, Int>) -> Unit,
    val offsets: Map<Int, Int> = mapOf(),
    val returnTo: UI? = null,
    val language: Language = player.language.child("kitpvp")
): UI {
    private val baseInventory: Inventory
        get() =
        Bukkit.createInventory(null, 9 * 6, language.getCmp("ui.layout.title"))
            .apply {
                // Separators
                addMarginItems(36..44, material = Material.LIGHT_BLUE_STAINED_GLASS_PANE, name = language.get("ui.layout.item.hotbar_sep"))
                addMarginItems(0..8, material = Material.LIGHT_BLUE_STAINED_GLASS_PANE, name = language.get("ui.layout.item.inventory_sep"))

                // Save
                setItem(0, ItemBuilder(Material.GREEN_STAINED_GLASS_PANE, language.getCmp("ui.layout.save"))
                    .guiItem {
                        val updated = buildMap {
                            for (i in 0..35) {
                                val offset = if (i < 9) 45 else 0
                                put(i, KitItem.fromItem(it.inventory.getItem(i + offset)) ?: continue)
                            }
                        }

                        val offsets = KitPreferenceUtils.calculateOffsets(kit.inventory, updated)

                        // Exit
                        returnTo?.open() ?: player.closeInventory()
                        onChoose(offsets)
                    }
                    .build().immovable())

                // Add armor items
                setItem(2, armorItem("helmet", kit.helmet, Material.ARMOR_STAND))
                setItem(3, armorItem("chestplate", kit.chestplate, Material.ARMOR_STAND))
                setItem(4, armorItem("leggings", kit.leggings, Material.ARMOR_STAND))
                setItem(5, armorItem("boots", kit.boots, Material.ARMOR_STAND))
                setItem(6, armorItem("offhand", kit.offhand, Material.ITEM_FRAME))

                // Populate with items
                val inventory = KitPreferenceUtils.applyOffsets(kit.inventory, offsets)
                for (i in 0..35) {
                    val offset = if (i < 9) 45 else 0

                    inventory.getOrDefault(i, null)?.let { item ->
                        setItem(i + offset, item.builder.build())
                    }
                }
            }

    private fun armorItem(key: String, item: KitItem?, default: Material): ItemStack {
        val nameCmp = language.getCmp("ui.layout.item.$key")
        val loreCmp = mutableListOf(language.getCmp("ui.layout.notice.immovable") as TextComponent)

        return item?.builder?.apply {
            name = nameCmp
            lore = loreCmp
        }?.build()?.immovable()
            ?: ItemBuilder(default, nameCmp, lore = loreCmp).build().immovable()
    }

    init {
        open()
    }

    override fun open() {
        player.playSound(player.location, Sound.BLOCK_SCAFFOLDING_BREAK, 5f, 0.5f)
        player.openInventory(baseInventory)
        player.inventory.clear()

        returnTo?.let { UIHandler.nonClosable[player.uniqueId] = it }
        UIHandler.nonCancelled.add(player.uniqueId)
    }
}