package de.c4vxl.kitpvp.utils

import de.c4vxl.gamelobby.Main
import de.c4vxl.gamemanager.gma.GMA
import de.c4vxl.gamemanager.gma.player.GMAPlayer.Companion.gma
import de.c4vxl.gamemanager.language.Language.Companion.language
import de.c4vxl.kitpvp.data.struct.kit.Kit
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.*

object TryOn {
    val players: MutableSet<UUID> = mutableSetOf()

    /**
     * Opens kit preview
     * @param player The player to open the kit preview for
     * @param kit The kit to preview
     */
    fun open(player: Player, kit: Kit) {
        if (!players.add(player.uniqueId))
            return

        // Send player into a queuing game
        // We do this to hide him from other players
        val game = GMA.createGame(1, 1, player.gma)
        player.gma.join(game)

        // Prepare player
        player.clearTitle()
        player.inventory.close()
        Bukkit.getScheduler().callSyncMethod(Main.instance) {
            player.sendActionBar(player.language.child("kitpvp").getCmp("tryon.title.exit_notice"))
        }

        // Equip kit
        kit.equip(player)
        kit.rules.giveEffects(player)

        // Blind player
        player.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS, 9999999, 255, false, false, false))

        // Play sound
        player.playSound(player.location, Sound.BLOCK_BEACON_POWER_SELECT, 5f, 2f)
    }
}