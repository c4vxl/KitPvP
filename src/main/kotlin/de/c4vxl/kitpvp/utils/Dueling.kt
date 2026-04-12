package de.c4vxl.kitpvp.utils

import de.c4vxl.gamemanager.gma.GMA
import de.c4vxl.gamemanager.gma.game.Game
import de.c4vxl.gamemanager.gma.game.type.GameID
import de.c4vxl.gamemanager.gma.game.type.GameSize
import de.c4vxl.gamemanager.gma.player.GMAPlayer.Companion.gma
import de.c4vxl.gamemanager.language.Language.Companion.language
import de.c4vxl.kitpvp.data.extensions.Extensions.kitData
import de.c4vxl.kitpvp.data.struct.kit.Kit
import org.bukkit.entity.Player

/**
 * Handles the creation and joining of duels
 */
object Dueling {
    /**
     * Creates a game for a duel
     * @param challenger The creator of the duel
     * @param challenged The challenged player
     * @param kit The kit used for the duel
     */
    private fun createDuel(challenger: Player, challenged: Player, kit: Kit): Game {
        val game = GMA.createGame(GameSize(2, 1), challenger.gma)
        game.kitData.challenged = challenged.gma
        game.kitData.kit = kit

        return game
    }

    /**
     * Creates a duel
     */
    fun duel(challenger: Player, opponent: Player, kit: Kit) {
        challenger.closeInventory()

        // Get languages
        val challengerLanguage = challenger.language.child("kitpvp")
        val opponentLanguage = opponent.language.child("kitpvp")

        // Challenger is in a game
        if (challenger.gma.isInGame) {
            challenger.sendMessage(challengerLanguage.getCmp("msg.duel.ask.error.self_already_playing", opponent.name))
            return
        }

        // Opponent is in a game
        if (opponent.gma.isInGame) {
            challenger.sendMessage(challengerLanguage.getCmp("msg.duel.ask.error.other_already_playing", opponent.name))
            return
        }

        // Create game
        val game = createDuel(challenger, opponent, kit)
        challenger.gma.join(game)

        // Send confirmation
        challenger.sendMessage(challengerLanguage.getCmp("msg.duel.ask.success", opponent.name))

        // Send request
        opponent.sendMessage(
            opponentLanguage.getCmp("msg.duel.request.1")
                .appendNewline().append(opponentLanguage.getCmp("msg.duel.request.2", challenger.name))
                .appendNewline().append(opponentLanguage.getCmp("msg.duel.request.3", kit.metadata.name))
                .appendNewline().append(opponentLanguage.getCmp("msg.duel.request.4"))
                .appendNewline().append(opponentLanguage.getCmp("msg.duel.request.5", game.id.asString))
        )
    }

    /**
     * Makes a player join a duel
     * @param player The player
     * @param duelID The id of the duel to join
     *
     * @return {@code true} if joined successfully
     */
    fun joinDuel(player: Player, duelID: String): Boolean {
        val duelGame = GMA.getGame(GameID.fromString(duelID)) ?: return false

        // Game is not a duel
        if (!duelGame.kitData.isDuel)
            return false

        // Player is not the challenged player
        if (duelGame.kitData.challenged != player.gma)
            return false

        // Join duel
        return player.gma.join(duelGame, true)
    }

    /**
     * Declines a duel
     * @param player The player declining the duel
     * @param duelID The id of the duel
     */
    fun declineDuel(player: Player, duelID: String): Boolean {
        val duelGame = GMA.getGame(GameID.fromString(duelID)) ?: return false

        if (player.gma.game == duelGame)
            return false

        // Game is not a duel
        if (!duelGame.kitData.isDuel)
            return false

        // Player is not the challenged player
        if (duelGame.kitData.challenged != player.gma)
            return false

        // Send message
        duelGame.owner?.bukkitPlayer?.let {
            it.sendMessage(it.language.child("kitpvp").getCmp("msg.duel.declined", player.name))
            it.gma.quit()
        }

        // Stop game
        duelGame.stop()
        return true
    }
}