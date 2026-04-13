package de.c4vxl.kitpvp.data.extensions

import de.c4vxl.gamemanager.gma.GMA
import de.c4vxl.gamemanager.gma.game.Game
import de.c4vxl.gamemanager.gma.game.type.GameID
import de.c4vxl.gamemanager.gma.game.type.GameSize
import de.c4vxl.kitpvp.data.struct.game.GameData
import de.c4vxl.kitpvp.data.struct.kit.Kit
import org.bukkit.World
import org.bukkit.entity.Player
import java.util.*

/**
 * Extends objects with extra data/functions
 */
object Extensions {
    val data = mutableMapOf<GameID, GameData>()
    private val lastKit = mutableMapOf<UUID, Kit>()
    private val lastGameSize = mutableMapOf<UUID, GameSize>()

    /**
     * Returns game kit data
     */
    val Game.kitData get() =
        data.computeIfAbsent(this.id) { GameData(this) }

    /**
     * Returns true if the kit was not created by a player
     */
    val Kit.isServerKit get() =
        this.metadata.createdBy.isBlank()

    /**
     * Holds the last kit a player has played
     */
    var Player.lastKit: Kit?
        get() = this@Extensions.lastKit[this.uniqueId]
        set(value) {
            value?.let { this@Extensions.lastKit[this.uniqueId] = it } ?:
            this@Extensions.lastKit.remove(this.uniqueId)
        }

    /**
     * Holds the last game size of the last game a player has played
     */
    var Player.lastGameSize: GameSize?
        get() = this@Extensions.lastGameSize[this.uniqueId]
        set(value) {
            value?.let { this@Extensions.lastGameSize[this.uniqueId] = it } ?:
            this@Extensions.lastGameSize.remove(this.uniqueId)
        }

    /**
     * Tries to find a game based on its game world
     */
    val World.game: Game? get() =
        // We know that the game id is part of the map name
        // The reason we need to use .contains is that the server might set a custom world prefix
        GMA.registeredGames.find { this.name.contains(it.id.asString) }
}