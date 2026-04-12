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
    var old: MutableMap<PotionEffectType, Pair<Int, Int>> = mutableMapOf(),
    val onChoose: (MutableMap<PotionEffectType, Pair<Int, Int>>) -> Unit,
    val allowDuration: Boolean = true,
    val language: Language = player.language.child("kitpvp")
) {
    private val title = language.getCmp(titleKey)

    private val effects =
        Gson().fromJson<Map<String, String>>(Main.instance.dataFolder.resolve("potionEffects.json").readText(), Map::class.java)
            .mapNotNull { (key, value) ->
                val type: PotionEffectType = PotionEffectType.values().find { it.key.key.lowercase() == key.lowercase() } ?: return@mapNotNull null
                val material: Material = Material.entries.find { it.name.lowercase() == value.lowercase() } ?: return@mapNotNull null

                type to material
            }.toMap()

    private val numPages: Int = (effects.size + 13) / 14

    private var page: Int = 0

    private val baseInventory: Inventory
        get() =
            Bukkit.createInventory(null, 9 * 6, title)
                .apply {
                    addMarginItems(0..17, 45..53, 36..44, 0..45 step 9, 17..53 step 9)

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

                    // Pagination items
                    if (numPages > 1) {
                        fun updatePage(change: Int) {
                            page += change

                            // Apply bounds
                            if (page > numPages - 1)
                                page = 0
                            if (page < 0)
                                page = numPages - 1

                            open()
                        }

                        setItem(
                            48, ItemBuilder(Material.ARROW, language.getCmp("ui.effect.item.previous"))
                            .guiItem { updatePage(-1) }.build()
                        )

                        setItem(
                            50, ItemBuilder(Material.ARROW, language.getCmp("ui.effect.item.next"))
                            .guiItem { updatePage(1) }.build()
                        )
                    }
                }

    private fun getItemsForPage(page: Int) =
        effects.toList().subList(
            (14 * page).coerceAtMost(effects.size),
            (14 * page + 14).coerceAtMost(effects.size)
        ).map { (effect, icon) ->
                ItemBuilder(
                    icon,
                    Component.translatable(effect.translationKey()),
                    lore = buildList {
                        val old = old.getOrDefault(effect, Pair(0, 0))
                        repeat(if (allowDuration) 7 else 4) { i -> add(
                            language.getCmp("ui.effect.item.lore.${i + 1}", old.first.toString(), old.second.toString()) as TextComponent
                        ) }
                    }.toMutableList()
                )
                    .guiItem { event ->
                        val change = if (event.isRightClick) -1 else 1
                        val current = old[effect] ?: Pair(0, 0)
                        val currentValue = current.let {
                            if (event.isShiftClick) it.second
                            else it.first
                        }
                        val changedValue = max(0, min(currentValue + change, 999))
                        val changed = if (event.isShiftClick) Pair(current.first, changedValue) else Pair(changedValue, current.second)

                        if (changed.first == 0 && changed.second == 0)
                            old.remove(effect)
                        else
                            old[effect] = changed

                        open()
                    }
                    .build()
            }

    private fun open(page: Int) {
        println(page)
        player.playSound(player.location, Sound.BLOCK_SCAFFOLDING_BREAK, 5f, 0.5f)
        player.openInventory(baseInventory.apply {
            getItemsForPage(page).forEach { addItem(it) }
        })
        player.inventory.clear()
    }

    init {
        open()
    }

    private fun open() {
        open(page)
    }
}