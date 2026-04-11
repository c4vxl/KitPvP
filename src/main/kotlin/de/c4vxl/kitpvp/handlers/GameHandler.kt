package de.c4vxl.kitpvp.handlers

import de.c4vxl.gamemanager.gma.event.game.GameEndEvent
import de.c4vxl.gamemanager.gma.event.game.GameStartEvent
import de.c4vxl.gamemanager.gma.event.game.GameStopEvent
import de.c4vxl.gamemanager.gma.event.game.GameWorldLoadedEvent
import de.c4vxl.gamemanager.gma.event.player.GamePlayerEquipEvent
import de.c4vxl.gamemanager.gma.event.player.GamePlayerLoseEvent
import de.c4vxl.gamemanager.gma.event.player.GamePlayerRespawnEvent
import de.c4vxl.gamemanager.gma.event.player.GamePlayerWinEvent
import de.c4vxl.gamemanager.gma.game.Game
import de.c4vxl.gamemanager.gma.player.GMAPlayer.Companion.gma
import de.c4vxl.kitpvp.Main
import de.c4vxl.kitpvp.data.extensions.Extensions.data
import de.c4vxl.kitpvp.data.extensions.Extensions.kitData
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.GameRules
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

class GameHandler : Listener {
    init {
        Bukkit.getPluginManager().registerEvents(this, Main.instance)
    }

    @EventHandler
    fun onGameStop(event: GameStopEvent) {
        data.remove(event.game.id)
    }

    @EventHandler
    fun onGameStart(event: GameStartEvent) {
        val game = event.game
        val kit = game.kitData.kit

        // Initialize rounds remaining flag
        game.kitData.roundsRemaining = kit?.rules?.numRounds ?: 1
    }

    @EventHandler
    fun onWorldLoaded(event: GameWorldLoadedEvent) {
        val kit = event.game.kitData.kit ?: return
        val rules = kit.rules
        val world = event.map.world ?: return

        // Set world game rules
        mapOf(
            // Kit rules based game rules
            listOf(GameRules.KEEP_INVENTORY) to rules.isKeepInventory,
            listOf(GameRules.ADVANCE_TIME) to rules.isAlwaysDay,
            listOf(GameRules.FALL_DAMAGE) to rules.isFallDamage,
            listOf(GameRules.BLOCK_DROPS) to rules.isBlockDrops,
            listOf(GameRules.MOB_DROPS, GameRules.ENTITY_DROPS) to rules.isEntityDrops,

            // Independent game rules
            listOf(GameRules.ADVANCE_WEATHER) to false
        ).forEach { (gameRules, value) -> gameRules.forEach { world.setGameRule(it, value) } }
    }

    @EventHandler
    fun onEquip(event: GamePlayerEquipEvent) {
        val kit = event.game.kitData.kit ?: return
        val player = event.player.bukkitPlayer

        // Don't re-equip the player on respawns if keep inventory is disabled in kit rules
        if (event.reason == GamePlayerEquipEvent.Reason.RESPAWN && !kit.rules.isKeepInventory)
            return

        // Reset player
        player.gma.reset()

        // Equip player
        kit.equip(player, event.game.kitData.getPlayerOffsets(player))
        kit.rules.giveEffects(player)

        // Set max health
        player.maxHealth = kit.rules.health
        player.health = player.maxHealth
    }

    /**
     * Resets all players of a game for the next round
     * @param game The game
     */
    private fun reset(game: Game) {
        val map = game.worldManager.map ?: return
        game.playerManager.alivePlayers.forEach { player ->
            // Teleport to spawn
            val team = player.team ?: return@forEach
            val spawn = map.getSpawnLocation(team.id) ?: map.world?.spawnLocation ?: return@forEach
            player.bukkitPlayer.teleport(spawn)

            // Equip players
            GamePlayerEquipEvent(player, game, GamePlayerEquipEvent.Reason.GAME_START)
                .callEvent()
        }
    }

    @EventHandler
    fun onPlayerRespawn(event: GamePlayerRespawnEvent) {
        val game = event.game.takeIf { it.isRunning } ?: return

        // Filter for teams that are still alive
        val aliveTeams = game.teamManager.teams.values.filter {
            it.players.none { m ->
                m.game != game                                                             // player has left the game
                        || m.isSpectating || m.bukkitPlayer.gameMode == GameMode.SPECTATOR // player is spectating
                        || m.bukkitPlayer.uniqueId == event.player.bukkitPlayer.uniqueId   // player just died
            }
        }

        // Still more than one team alive
        // Let player spectate
        if (aliveTeams.size > 1) {
            event.player.bukkitPlayer.gameMode = GameMode.SPECTATOR
            return
        }

        // Only one team alive -> this team won
        val winner = aliveTeams.firstOrNull() ?: return
        game.kitData.roundsWon[winner.id] = game.kitData.roundsWon.getOrDefault(winner.id, 0) + 1

        // Advance to next round
        game.kitData.roundsRemaining -= 1
        reset(game)

        // Still rounds remaining
        if (game.kitData.roundsRemaining >= 1)
            return

        // No more rounds remaining
        // End the game

        // Evaluate winning teams
        val roundsWon = game.kitData.roundsWon
            .mapNotNull { (game.teamManager.teams[it.key] ?: return@mapNotNull null) to it.value }
            .sortedByDescending { it.second }

        val highestNumWins = roundsWon.maxOfOrNull { it.second } ?: 0
        val winnerTeams = roundsWon.filter { it.second == highestNumWins }.map { it.first }.toSet()
        val otherTeams = event.game.teamManager.teams.values.filterNot { it in winnerTeams }

        if (roundsWon.isNotEmpty()) {
            // Call win/loose events
            winnerTeams.forEach { it.players.forEach { player -> GamePlayerWinEvent(player, game).callEvent() } }
            otherTeams.forEach { it.players.forEach { player -> GamePlayerLoseEvent(player, game).callEvent() } }
        }

        // End game
        GameEndEvent(
            game,
            winnerTeams.toList(),
            otherTeams
        ).callEvent()

        // Stop game
        game.stop()
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onEnd(event: GameEndEvent) {
        val players = buildMap {
            event.winnerTeams.forEach { team -> team.players.forEach { put(it, true) } }
            event.teamsLost.forEach { team -> team.players.forEach { put(it, false) } }
        }
        
        val numRounds = event.game.kitData.kit?.rules?.numRounds

        players.forEach { (player, hasWon) ->
            val numWon = event.game.kitData.roundsWon.getOrDefault(player.team?.id, 0)
            val language = player.language.child("kitpvp")

            player.bukkitPlayer.sendMessage(
                language.getCmp("msg.end.1")
                    .appendNewline()
                    .append(language.getCmp("msg.end.2", numWon.toString(), numRounds.toString()))
                    .appendNewline()
                    .append(language.getCmp("msg.end.3.${if (hasWon) "won" else "lost"}"))
            )
            player.bukkitPlayer.clearTitle()
        }
    }
}