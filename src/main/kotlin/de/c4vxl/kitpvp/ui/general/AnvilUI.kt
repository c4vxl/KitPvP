package de.c4vxl.kitpvp.ui.general

import de.c4vxl.gamemanager.language.Language
import de.c4vxl.gamemanager.language.Language.Companion.language
import de.c4vxl.gamemanager.utils.ItemBuilder
import de.c4vxl.kitpvp.utils.Item
import de.c4vxl.kitpvp.utils.Item.guiItem
import de.c4vxl.kitpvp.utils.Item.onDrop
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.MenuType
import org.bukkit.inventory.view.AnvilView

/**
 * A base anvil ui
 */
@Suppress("UnstableApiUsage")
open class AnvilUI(
    private val player: Player,
    private val titleTranslationKey: String,
    private val confirmTranslationKey: String,
    val onInput: (String) -> Unit,
    private val language: Language = player.language.child("kitpvp")
) {
    val baseView: AnvilView
        get() = MenuType.ANVIL.builder()
            .checkReachable(false)
            .location(player.location)
            .title(language.getCmp(titleTranslationKey))
            .build(player)
            .apply {
                bypassEnchantmentLevelRestriction(true)

                topInventory.setItem(0, Item.marginItem(Material.NAME_TAG))

                topInventory.setItem(1, ItemBuilder(
                    Material.GREEN_STAINED_GLASS_PANE,
                    language.getCmp(confirmTranslationKey)
                )
                    .guiItem {
                        val view = it.view as? AnvilView
                        onInput(view?.renameText ?: "")
                    }
                    .onDrop { it.itemDrop.remove() }
                    .build())
            }

    init {
        baseView.open()
    }
}