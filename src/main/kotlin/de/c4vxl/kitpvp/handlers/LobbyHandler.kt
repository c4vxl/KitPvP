package de.c4vxl.kitpvp.handlers

import de.c4vxl.gamelobby.events.lobby.LobbyPlayerEquipEvent
import de.c4vxl.gamemanager.language.Language.Companion.language
import de.c4vxl.gamemanager.utils.ItemBuilder
import de.c4vxl.kitpvp.Main
import de.c4vxl.kitpvp.ui.KitEditor
import de.c4vxl.kitpvp.utils.Item.enchantmentGlow
import de.c4vxl.kitpvp.utils.Item.onRightClick
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

/**
 * Intercepts the GameLobby plugin and adds KitPvP-specific logic
 */
class LobbyHandler : Listener {
    init {
        Bukkit.getPluginManager().registerEvents(this, Main.instance)
    }

    @EventHandler
    fun onEquip(event: LobbyPlayerEquipEvent) {
        val inv = event.player.inventory
        val lang = event.player.language.child("kitpvp")

        inv.setItem(4, ItemBuilder(
            Material.BOOK,
            lang.getCmp("lobby.item.kit_editor"),
            enchantments = mutableMapOf(Enchantment.UNBREAKING to 1)
        )
            .onRightClick { KitEditor(it.player, kitName = "Unnamed Kit").open() }
            .build()
            .enchantmentGlow()
        )
    }
}