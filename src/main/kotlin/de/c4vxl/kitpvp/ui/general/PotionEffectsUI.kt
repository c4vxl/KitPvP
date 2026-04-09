package de.c4vxl.kitpvp.ui.general

import com.google.gson.Gson
import de.c4vxl.gamemanager.language.Language
import de.c4vxl.gamemanager.language.Language.Companion.language
import de.c4vxl.gamemanager.utils.ItemBuilder
import de.c4vxl.kitpvp.Main
import de.c4vxl.kitpvp.utils.Item.addMarginItems
import de.c4vxl.kitpvp.utils.Item.guiItem
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.potion.PotionEffectType
import kotlin.math.max
import kotlin.math.min

/**
 * Ui for choosing potion effects
 */
class PotionEffectsUI(
    val player: Player,
    titleKey: String,
    var old: MutableMap<PotionEffectType, Int> = mutableMapOf(),
    val onChoose: (MutableMap<PotionEffectType, Int>) -> Unit,
    val language: Language = player.language.child("kitpvp")
) {
    private val title = language.getCmp(titleKey)

    private val effects =
        Gson().fromJson<Map<String, String>>(Main.instance.dataFolder.resolve("potionEffects.json").readText(), Map::class.java)
            .mapNotNull { (key, value) ->
                val type: PotionEffectType = PotionEffectType.values().find { it.name.lowercase() == key.lowercase() } ?: return@mapNotNull null
                val material: Material = Material.entries.find { it.name.lowercase() == value.lowercase() } ?: return@mapNotNull null

                type to material
            }.toMap()

    private val baseInventory: Inventory
        get() =
            Bukkit.createInventory(null, 9 * 5, title)
                .apply {
                    addMarginItems(0..17, 36..44, 0..27 step 9, 17..35 step 9)

                    // Back item
                    setItem(8, ItemBuilder(Material.GREEN_STAINED_GLASS_PANE, language.getCmp("ui.effect.item.save"))
                        .guiItem { onChoose(old) }
                        .build())

                    // Reset item
                    setItem(0, ItemBuilder(Material.GRINDSTONE, language.getCmp("ui.effect.item.reset"))
                        .guiItem { event ->
                            old = mutableMapOf()
                            (event.whoClicked as Player).playSound(event.whoClicked.location, Sound.BLOCK_GRINDSTONE_USE, 3f, 1f)
                            open()
                        }
                        .build())

                    // Add items
                    effects.forEach { (effect, icon) ->
                        addItem(ItemBuilder(
                            icon,
                            Component.translatable(effect.translationKey()),
                            lore = buildList {
                                repeat(4) { i -> add(
                                    language.getCmp("ui.effect.item.lore.${i + 1}", old.getOrDefault(effect, 0).toString()) as TextComponent
                                ) }
                            }.toMutableList()
                        )
                            .guiItem {
                                val change = if (it.isRightClick) -1 else 1
                                val current = old.getOrDefault(effect, 0)
                                val changed = max(0, min(current + change, 999))
                                if (changed == 0)
                                    old.remove(effect)
                                else
                                    old[effect] = changed

                                open()
                            }
                            .build())
                    }
                }

    init {
        open()
    }

    private fun open() {
        player.playSound(player.location, Sound.BLOCK_SCAFFOLDING_BREAK, 5f, 0.5f)
        player.openInventory(baseInventory)
        player.inventory.clear()
    }
}