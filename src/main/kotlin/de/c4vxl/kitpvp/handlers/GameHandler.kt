package de.c4vxl.kitpvp.handlers

import de.c4vxl.gamemanager.gma.event.game.*
import de.c4vxl.gamemanager.gma.event.player.*
import de.c4vxl.gamemanager.gma.game.Game
import de.c4vxl.gamemanager.gma.player.GMAPlayer.Companion.gma
import de.c4vxl.kitpvp.Main
import de.c4vxl.kitpvp.data.extensions.Extensions.data
import de.c4vxl.kitpvp.data.extensions.Extensions.kitData
import de.c4vxl.kitpvp.data.extensions.Extensions.lastKit
import it.unimi.dsi.fastutil.ints.Int2IntFunctions.UnmodifiableFunction
import net.kyori.adventure.title.TitlePart
import org.bukkit.*
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

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onJoin(event: GamePlayerJoinedEvent) {
        val kit = event.game.kitData.kit
        val lastKit = event.player.bukkitPlayer.lastKit

        // We don't care about games used for TryOn
        if (event.game.kitData.isTryOn)
            return

        // Game doesn't have a kit
        // Try to force the players last kit
        if (kit == null && lastKit != null) {
            event.game.kitData.kit = lastKit
            return
        }

        // Kit is not null
        // Don't need to stop the game
        if (kit != null)
            return

        // No kit possible
        // Stop game
        event.game.stop()
        Bukkit.getScheduler().callSyncMethod(Main.instance) {
            event.player.bukkitPlayer.sendActionBar(event.player.language.child("kitpvp").getCmp("msg.error.no_kit"))
        }
    }

    @EventHandler
    fun onGameStarted(event: GameStartedEvent) {
        val kit = event.game.kitData.kit ?: return
        val map = event.game.worldManager.map

        event.game.players.forEach { player ->
            val lang = player.language.child("kitpvp")
            player.bukkitPlayer.sendMessage(
                lang.getCmp("msg.start.1")
                    .appendNewline().append(lang.getCmp("msg.start.2", kit.metadata.name))

                    .let {
                        val creator = try { kit.metadata.creatorPlayer.name ?: "" } catch (_: Exception) { "" }

                        if (creator.isNotBlank())
                            it.appendNewline().append(lang.getCmp("msg.start.3", creator))
                        else
                            it
                    }

                    .appendNewline().append(lang.getCmp("msg.start.4", kit.rules.numRounds.toString()))
                    .appendNewline().append(lang.getCmp("msg.start.5", event.game.size.toString()))
                    .appendNewline().append(lang.getCmp("msg.start.6", map?.name ?: "/", map?.builders?.joinToString(", ") ?: "/"))
                    .appendNewline()
                    .appendNewline().append(lang.getCmp("msg.start.7"))
            )

            player.bukkitPlayer.sendTitlePart(TitlePart.TITLE, lang.getCmp("title.game.start.title"))
            player.bukkitPlayer.sendTitlePart(TitlePart.SUBTITLE, lang.getCmp("title.game.start.subtitle"))
            player.bukkitPlayer.playSound(player.bukkitPlayer.location, Sound.BLOCK_BEACON_POWER_SELECT, 5f, 1f)
        }
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
            listOf(GameRules.ADVANCE_WEATHER) to false,
            listOf(GameRules.LOCATOR_BAR) to false
        ).forEach { (gameRules, value) -> gameRules.forEach { world.setGameRule(it, value) } }

        // Set difficulty
        world.difficulty = Difficulty.NORMAL
        world.time = 6000
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
        val kit = game.kitData.kit ?: return
        val roundsTotal = kit.rules.numRounds
        val roundsRemaining = game.kitData.roundsRemaining
        val roundsPlayed = roundsTotal - roundsRemaining

        val map = game.worldManager.map ?: return
        game.playerManager.alivePlayers.forEach { player ->
            // Teleport to spawn
            val team = player.team ?: return@forEach
            val spawn = map.getSpawnLocation(team.id) ?: map.world?.spawnLocation ?: return@forEach
            player.bukkitPlayer.teleport(spawn)

            // Equip players
            GamePlayerEquipEvent(player, game, GamePlayerEquipEvent.Reason.GAME_START)
                .callEvent()

            // Play sound
            player.bukkitPlayer.playSound(player.bukkitPlayer.location, Sound.BLOCK_BEACON_POWER_SELECT, 5f, 1f)

            // Send title
            val lang = player.language.child("kitpvp")
            player.bukkitPlayer.sendTitlePart(TitlePart.TITLE, lang.getCmp("title.round.start.title", roundsPlayed.toString(), roundsTotal.toString()))
            player.bukkitPlayer.sendTitlePart(TitlePart.SUBTITLE, lang.getCmp("title.round.start.subtitle", roundsPlayed.toString(), roundsTotal.toString()))
        }

        // Reset map
        if (kit.rules.isResetMap)
            MapHandler.reset(game)
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
        Bukkit.getScheduler().callSyncMethod(Main.instance) {
            reset(game)
        }

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