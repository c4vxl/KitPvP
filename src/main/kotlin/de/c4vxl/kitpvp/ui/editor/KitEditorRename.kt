package de.c4vxl.kitpvp.ui.editor

import de.c4vxl.kitpvp.data.struct.kit.Kit
import de.c4vxl.kitpvp.handlers.UIHandler
import de.c4vxl.kitpvp.ui.general.AnvilUI
import de.c4vxl.kitpvp.ui.type.UI
import org.bukkit.Sound
import org.bukkit.entity.Player

object KitEditorRename {
    fun open(player: Player, kit: Kit, onDone: (Kit) -> Unit, returnTo: UI? = null) {
        val ui = AnvilUI(
            player,
            "editor.page.rename.title",
            "editor.page.rename.confirm",
            {
                kit.metadata.name = it.takeIf { it.isNotBlank() } ?: kit.metadata.name
                player.playSound(player.location, Sound.ENTITY_PLAYER_LEVELUP, 5f, 1f)

                UIHandler.nonClosable.remove(player.uniqueId)

                onDone(kit)
            },
            kit.metadata.name
        )

        UIHandler.nonClosable[player.uniqueId] = returnTo ?: ui
    }
}