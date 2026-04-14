package de.c4vxl.kitpvp.handlers

import de.c4vxl.gamelobby.events.lobby.LobbyPlayerEquipEvent
import de.c4vxl.gamelobby.lobby.Lobby.isInLobby
import de.c4vxl.gamemanager.gma.event.game.GameEndEvent
import de.c4vxl.gamemanager.gma.event.game.GameStartedEvent
import de.c4vxl.gamemanager.gma.event.player.GamePlayerEquipEvent
import de.c4vxl.gamemanager.gma.event.player.GamePlayerJoinedEvent
import de.c4vxl.gamemanager.gma.player.GMAPlayer.Companion.gma
import de.c4vxl.gamemanager.language.Language.Companion.language
import de.c4vxl.kitpvp.Main
import de.c4vxl.kitpvp.data.PlayerKitData
import de.c4vxl.kitpvp.data.extensions.Extensions.kitData
import me.lucko.spark.paper.common.tick.SimpleTickReporter
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.scoreboard.Criteria
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Team

/**
 * Displays a scoreboard on the side of games
 */
class ScoreboardHandler : Listener {
    init {
        Bukkit.getPluginManager().registerEvents(this, Main.instance)
    }

    private fun createObjective(player: Player) =
        "kitpvp_${player.uniqueId}".let {
            // Unregister old objective if it exists
            player.scoreboard.getObjective(it)?.unregister()

            // Create objective
            player.scoreboard.registerNewObjective(it, Criteria.DUMMY, player.language.child("kitpvp").getCmp("scoreboard.title"))
        }.apply { displaySlot = DisplaySlot.SIDEBAR }

    /**
     * Displays a scoreboard to a player
     * @param player The player to display the scoreboard to
     * @param lines The lines to display
     */
    private fun display(player: Player, vararg lines: Component) {
        // Disable collision if in lobby
        if (player.isInLobby)
            player.scoreboard.registerNewTeam(player.name).apply {
                setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER)
                addPlayer(player)
            }

        // Create objective
        val objective = createObjective(player)

        // Add lines
        lines.reversed().forEachIndexed { i, component ->
            objective.getScore(LegacyComponentSerializer.legacySection().serialize(component)).score = i
        }
    }

    @EventHandler
    fun onGameJoin(event: GamePlayerJoinedEvent) {
        val kit = event.game.kitData.kit ?: return
        val lang = event.player.language.child("kitpvp")
        
        display(
            event.player.bukkitPlayer,
            Component.empty(),
            lang.getCmp("scoreboard.game.lobby.1"),
            lang.getCmp("scoreboard.game.lobby.2", kit.metadata.name),
            Component.text(" "),
            lang.getCmp("scoreboard.game.lobby.3"),
            lang.getCmp("scoreboard.game.lobby.4", kit.rules.numRounds.toString()),
        )
    }

    @EventHandler
    fun onEquip(event: GamePlayerEquipEvent) {
        val game = event.game
        val kit = game.kitData.kit ?: return
        val lang = event.player.language.child("kitpvp")

        val roundsTotal = kit.rules.numRounds
        val roundsRemaining = game.kitData.roundsRemaining
        val roundsPlayed = roundsTotal - roundsRemaining
        val roundsWon = game.kitData.roundsWon.getOrDefault(event.player.team?.id, 0)
        val map = game.worldManager.map?.name

        Bukkit.getScheduler().callSyncMethod(Main.instance) {
            display(
                event.player.bukkitPlayer,
                Component.empty(),
                lang.getCmp("scoreboard.game.running.1"),
                lang.getCmp("scoreboard.game.running.2", kit.metadata.name),
                Component.text(" "),
                lang.getCmp("scoreboard.game.running.3"),
                lang.getCmp("scoreboard.game.running.4", roundsPlayed.toString(), roundsTotal.toString()),
                Component.text("  "),
                lang.getCmp("scoreboard.game.running.5"),
                lang.getCmp("scoreboard.game.running.6", roundsWon.toString()),
                Component.text("   "),
                lang.getCmp("scoreboard.game.running.7"),
                lang.getCmp("scoreboard.game.running.8", map ?: "/"),
            )
        }
    }

    @EventHandler
    fun onLobby(event: LobbyPlayerEquipEvent) {
        if (event.player.gma.isInGame)
            return

        val lang = event.player.language.child("kitpvp")

        Bukkit.getScheduler().callSyncMethod(Main.instance) {
            display(
                event.player,
                Component.empty(),
                lang.getCmp("scoreboard.lobby.1", event.player.name),
                Component.text(" "),
                lang.getCmp("scoreboard.lobby.2"),
                lang.getCmp("scoreboard.lobby.3", PlayerKitData.getKits(event.player, false).size.toString(), PlayerKitData.numKits.toString())
            )
        }
    }
}