package de.c4vxl.kitpvp.handlers

import de.c4vxl.gamelobby.events.lobby.LobbyPlayerEquipEvent
import de.c4vxl.gamelobby.lobby.Lobby
import de.c4vxl.gamelobby.lobby.Lobby.isInLobby
import de.c4vxl.gamemanager.gma.event.player.GamePlayerJoinedEvent
import de.c4vxl.gamemanager.gma.event.player.GamePlayerQuitEvent
import de.c4vxl.gamemanager.gma.player.GMAPlayer.Companion.gma
import de.c4vxl.gamemanager.language.Language.Companion.language
import de.c4vxl.gamemanager.utils.ItemBuilder
import de.c4vxl.kitpvp.Main
import de.c4vxl.kitpvp.data.extensions.Extensions.kitData
import de.c4vxl.kitpvp.data.extensions.Extensions.lastKit
import de.c4vxl.kitpvp.ui.kit.KitUI
import de.c4vxl.kitpvp.ui.queue.GameQueueUI
import de.c4vxl.kitpvp.utils.Item.enchantmentGlow
import de.c4vxl.kitpvp.utils.Item.onRightClick
import de.c4vxl.kitpvp.utils.TryOn
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.PlayerQuitEvent

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

        inv.clear()

        inv.setItem(7, ItemBuilder(
            Material.BOOK,
            lang.getCmp("lobby.item.kit_editor.name"),
            enchantments = mutableMapOf(Enchantment.UNBREAKING to 1)
        )
            .onRightClick {
                if (!event.player.isInLobby)
                    return@onRightClick

                KitUI(event.player, KitUI.Mode.EDIT, { kit -> TryOn.open(event.player, kit) })
            }
            .build()
            .enchantmentGlow()
        )

        inv.setItem(1, ItemBuilder(
            Material.IRON_SWORD,
            lang.getCmp("lobby.item.queue.name")
        )
            .onRightClick {
                if (!event.player.isInLobby)
                    return@onRightClick

                GameQueueUI(event.player)
            }
            .build())
    }

    @EventHandler
    fun onUIClose(event: InventoryCloseEvent) {
        val player = event.player as? Player ?: return
        if (!player.isInLobby) return

        // Only if inventory is actually being closed
        if (event.reason == InventoryCloseEvent.Reason.OPEN_NEW)
            return

        // Not on player inventories
        // InventoryType.CRAFTING because the player inv sometimes counts as "CRAFTING"
        // This isn't a problem as of right now, because no UI uses a crafting inv
        if (listOf(InventoryType.PLAYER, InventoryType.CRAFTING).contains(event.inventory.type))
            return

        // Return if non-closable
        if (UIHandler.nonClosable.contains(event.player.uniqueId))
            return

        if (player.gma.isInGame) {
            // Equip lobby items
            event.player.location.let {
                // Fake another join event to equip player with lobby-queue items
                GamePlayerJoinedEvent(player.gma, player.gma.game!!)
                    .callEvent()
                player.teleport(it)
            }
        } else {
            // Equip lobby items
            LobbyPlayerEquipEvent(player).callEvent()
        }
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        event.player.lastKit = null
    }

    @EventHandler
    fun onGameQuit(event: GamePlayerQuitEvent) {
        event.player.bukkitPlayer.lastKit = event.game.kitData.kit
    }
}