package de.c4vxl.kitpvp.ui.editor

import com.google.gson.Gson
import de.c4vxl.gamemanager.language.Language
import de.c4vxl.gamemanager.language.Language.Companion.language
import de.c4vxl.gamemanager.utils.ItemBuilder
import de.c4vxl.kitpvp.Main
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.entity.Player

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
}