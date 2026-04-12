package de.c4vxl.kitpvp.handlers

import de.c4vxl.gamelobby.events.queue.LobbyPlayerQueueJoinedEvent
import de.c4vxl.gamemanager.gma.GMA
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
import org.bukkit.Sound
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class QueueHandler : Listener {
    init {
        Bukkit.getPluginManager().registerEvents(this, Main.instance)

        var i = 0
        Bukkit.getScheduler().runTaskTimer(Main.instance, Runnable {
            GMA.registeredGames.forEach { game ->
                if (!game.isQueuing)
                    return@forEach

                game.players.forEach {
                    if (game.kitData.isTryOn)
                        it.bukkitPlayer.sendActionBar(it.language.child("kitpvp").getCmp("tryon.title.exit_notice"))

                    else
                        it.bukkitPlayer.sendActionBar(it.language.child("kitpvp").getCmp("queue.msg.queuing",
                            ".".repeat(i + 1), (game.players.size - 1).toString(), (game.size.maxPlayers - 1).toString()
                        ))
                }
            }

            i = if (i < 2) i+1 else 0
        }, 0, 20)
    }

    @EventHandler
    fun onGameQueue(event: LobbyPlayerQueueJoinedEvent) {
        val player = event.player
        val lang = player.language.child("kitpvp")
        val game = player.gma.game ?: return

        val kitChooserSlot = if (game.size.teamSize == 1) 1 else 4

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

        player.playSound(player.location, Sound.BLOCK_BEACON_POWER_SELECT, 5f, 1f)
    }
}