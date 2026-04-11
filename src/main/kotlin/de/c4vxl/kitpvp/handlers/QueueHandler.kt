package de.c4vxl.kitpvp.handlers

import de.c4vxl.gamelobby.events.queue.LobbyPlayerQueueJoinedEvent
import de.c4vxl.gamemanager.gma.player.GMAPlayer.Companion.gma
import de.c4vxl.gamemanager.language.Language.Companion.language
import de.c4vxl.gamemanager.utils.ItemBuilder
import de.c4vxl.kitpvp.Main
import de.c4vxl.kitpvp.data.extensions.Extensions.kitData
import de.c4vxl.kitpvp.ui.kit.KitLayout
import de.c4vxl.kitpvp.utils.Item.enchantmentGlow
import de.c4vxl.kitpvp.utils.Item.onRightClick
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class QueueHandler : Listener {
    init {
        Bukkit.getPluginManager().registerEvents(this, Main.instance)
    }

    @EventHandler
    fun onGameQueue(event: LobbyPlayerQueueJoinedEvent) {
        val player = event.player
        val lang = player.language.child("kitpvp")
        val game = player.gma.game ?: return

        val kitChooserSlot = if (game.size.teamSize == 1) 4 else 5

        player.inventory.setItem(kitChooserSlot, ItemBuilder(
            Material.BOOK,
            lang.getCmp("queue.item.layout_editor")
        )
            .onRightClick {
                val kit = game.kitData.kit ?: return@onRightClick
                KitLayout(player, kit, { game.kitData.offsets[player] = it }, game.kitData.getPlayerOffsets(player))
            }
            .build()
            .enchantmentGlow()
        )
    }
}