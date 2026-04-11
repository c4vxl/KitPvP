package de.c4vxl.kitpvp.data.extensions

import de.c4vxl.gamemanager.gma.game.Game
import de.c4vxl.gamemanager.gma.game.type.GameID
import de.c4vxl.kitpvp.data.struct.game.GameData
import de.c4vxl.kitpvp.data.struct.kit.Kit

/**
 * Extends objects with extra data/functions
 */
object Extensions {
    val data = mutableMapOf<GameID, GameData>()

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
}