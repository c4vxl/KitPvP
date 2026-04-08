package de.c4vxl.kitpvp.ui.editor.type

import com.google.gson.Gson
import de.c4vxl.gamemanager.language.Language
import de.c4vxl.gamemanager.language.Language.Companion.language
import de.c4vxl.gamemanager.utils.ItemBuilder
import de.c4vxl.kitpvp.Main
import de.c4vxl.kitpvp.utils.Item.guiItem
import de.c4vxl.kitpvp.utils.Item.onDrop
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * Handles the kiteditor.json config
 */
object KitEditorItems {
    /**
     * Holds the config file
     */
    val file
        get() = Main.instance.dataFolder.resolve("kiteditor.json")

    /**
     * Returns an ItemBuilder for an item
     * @param material The material
     * @param language The language to display the item name in
     */
    fun item(material: Material, language: Language) =
        ItemBuilder(
            material,
            Component.translatable(material.translationKey()).color(NamedTextColor.WHITE),
            lore = mutableListOf(language.getCmp("editor.item.equip.lore") as TextComponent)
        )

    /**
     * Returns the items per section from the config
     * @param player The player that requested the item list
     */
    private fun getSections(player: Player): Map<String, List<ItemBuilder>> {
        val materials = Gson().fromJson<Map<String, List<String>>>(file.readText(), Map::class.java)

        return materials.mapValues { (section, names) ->
            names.mapNotNull { name ->
                val material = Material.entries.find { it.name == name } ?: return@mapNotNull null
                return@mapNotNull item(material, player.language.child("kitpvp"))
            }
        }
    }

    /**
     * Returns the items of a specific section
     * @param player The player
     * @param player The player that requested the item list
     * @param section The specific section
     */
    fun getItems(player: Player, section: String) =
        getSections(player).getOrDefault(section, emptyList())

    /**
     * Returns an editable item
     * @param material The material of the item
     * @param language The language the item should be translated in to
     */
    fun editableItem(material: Material, language: Language): ItemStack =
        ItemBuilder(
            material,
            Component.translatable(material.translationKey()),
            lore = mutableListOf(
                Component.empty(),
                language.getCmp("editor.item.inv.lore.1") as TextComponent,
                language.getCmp("editor.item.inv.lore.2") as TextComponent,
                language.getCmp("editor.item.inv.lore.3") as TextComponent,
            )
        )
            .guiItem { event ->
                event.isCancelled = true

                // Clone with right click
                if (event.isRightClick && !event.isShiftClick)
                    event.whoClicked.setItemOnCursor(event.currentItem?.clone())

                // Enable pickup with left click
                if (event.isLeftClick) {
                    event.whoClicked.setItemOnCursor(event.currentItem)
                    event.currentItem = null
                }

                // Destroy on drop
                if (event.action.name.contains("DROP"))
                    event.currentItem = null
            }
            .onDrop { event ->
                event.isCancelled = false
                event.itemDrop.remove()
            }
            .build()
}