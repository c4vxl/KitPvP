package de.c4vxl.kitpvp.ui.editor

import de.c4vxl.gamemanager.utils.ItemBuilder
import de.c4vxl.kitpvp.data.struct.kit.rule.KitGameRule
import de.c4vxl.kitpvp.data.struct.kit.rule.KitGameRules
import de.c4vxl.kitpvp.handlers.UIHandler
import de.c4vxl.kitpvp.ui.general.PotionEffectsUI
import de.c4vxl.kitpvp.ui.type.UI
import de.c4vxl.kitpvp.utils.Item.addMarginItems
import de.c4vxl.kitpvp.utils.Item.guiItem
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.minecraft.world.item.alchemy.Potion
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import kotlin.math.max
import kotlin.math.min

/**
 * The Kit editor edit item gui
 */
class KitEditorGameRules(
    val editor: KitEditor
) : UI {
    private val title = editor.language.getCmp("editor.page.rules.title", editor.kit.metadata.name)
    private var returnToEditor = true

    private val baseInventory: Inventory
        get() =
            Bukkit.createInventory(null, 9 * 5, title)
                .apply {
                    addMarginItems(0..8, 36..44, 0..27 step 9, 8..35 step 9)

                    // Save
                    setItem(8, ItemBuilder(Material.GREEN_STAINED_GLASS_PANE, editor.language.getCmp("editor.page.rules.save"))
                        .guiItem {
                            if (returnToEditor)
                                editor.open()
                            else
                                open()
                        }
                        .build())

                    setItem(36, ItemBuilder(Material.BARRIER, editor.language.getCmp("editor.page.rules.reset"))
                        .guiItem {
                            editor.kit.rules = KitGameRules()
                            open()
                            editor.player.stopAllSounds()
                            editor.player.playSound(editor.player.location, Sound.BLOCK_GRINDSTONE_USE, 5f, 1f)
                        }
                        .build())


                }

    @Suppress("UNCHECKED_CAST")
    private fun <T : Any> createGameRuleItem(rule: KitGameRule, value: T, newValue: (T) -> Unit): ItemStack {
        return ItemBuilder(
            rule.icon,
            editor.language.getCmp(rule.getNameKey(value), value.toString()),
            lore = buildList {
                add(editor.language.getCmp("rule.${rule.name.lowercase()}.desc") as TextComponent)
                add(Component.empty())
                addAll(rule.getLore(editor.language, value))
            }.toMutableList()
        )
            .guiItem { event ->
                event.isCancelled = true

                when (rule.type) {
                    Boolean::class.java -> {
                        if (event.isLeftClick || event.isRightClick)
                            newValue(!(value as Boolean) as T)
                    }

                    Int::class.java -> {
                        if (event.isLeftClick)
                            newValue(((value as Int) + 1) as T)

                        if (event.isRightClick)
                            newValue(((value as Int) - 1) as T)
                    }

                    Double::class.java -> {
                        if (event.isLeftClick)
                            newValue(((value as Double) + 0.5) as T)

                        if (event.isRightClick)
                            newValue(((value as Double) - 0.5) as T)
                    }

                    Potion::class.java -> {
                        returnToEditor = false
                        PotionEffectsUI(
                            editor.player,
                            "editor.page.rules.title",
                            editor.kit.rules.activeEffectsMap.mapValues { Pair(it.value, 0) }.toMutableMap(),
                            { effects ->
                                editor.kit.rules.activeEffectsMap = effects.mapValues { it.value.first }.toMutableMap()
                                open()
                            },
                            false
                        )
                        UIHandler.nonClosable[editor.player.uniqueId] = this@KitEditorGameRules
                        return@guiItem
                    }
                }

                (event.whoClicked as Player).playSound(event.whoClicked.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 3f, 1f)
                open()
            }
            .build()
    }

    init {
        open()
    }

    private fun withItems() =
        baseInventory.apply {
            addItem(createGameRuleItem(KitGameRule.ALWAYS_DAY, editor.kit.rules.isAlwaysDay) { editor.kit.rules.isAlwaysDay = it })

            addItem(createGameRuleItem(KitGameRule.KEEP_INVENTORY, editor.kit.rules.isKeepInventory) { editor.kit.rules.isKeepInventory = it })

            addItem(createGameRuleItem(KitGameRule.FALL_DAMAGE, editor.kit.rules.isFallDamage) { editor.kit.rules.isFallDamage = it })

            addItem(createGameRuleItem(KitGameRule.ALLOW_BLOCK_BREAKING, editor.kit.rules.isAllowBlockBreaking) { editor.kit.rules.isAllowBlockBreaking = it })

            addItem(createGameRuleItem(KitGameRule.ITEM_DROP, editor.kit.rules.isItemDrop) { editor.kit.rules.isItemDrop = it })

            addItem(createGameRuleItem(KitGameRule.EXPLOSION_DAMAGE, editor.kit.rules.isExplosionDamage) { editor.kit.rules.isExplosionDamage = it })

            addItem(createGameRuleItem(KitGameRule.NUM_ROUNDS, editor.kit.rules.numRounds) {
                editor.kit.rules.numRounds = max(1, min(it, 10))
            })

            addItem(createGameRuleItem(KitGameRule.HEALTH, editor.kit.rules.health) {
                editor.kit.rules.health = max(0.5, min(999.0, it))
            })

            addItem(createGameRuleItem(KitGameRule.ACTIVE_EFFECTS, editor.kit.rules.activeEffects) {})
        }

    override fun open() {
        returnToEditor = true
        editor.player.playSound(editor.player.location, Sound.BLOCK_SCAFFOLDING_BREAK, 5f, 0.5f)
        editor.player.openInventory(withItems())
        editor.player.inventory.clear()

        UIHandler.nonClosable[editor.player.uniqueId] = editor
    }
}