package de.c4vxl.kitpvp.ui.general

import de.c4vxl.kitpvp.ui.type.UI
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player

/**
 * UI for searching for other players
 */
open class PlayerSearchUI(
    private val player: Player,
    private val onlyConnected: Boolean = true,
    onInput: (OfflinePlayer?) -> Unit,
    returnTo: UI? = null,
) : AnvilUI(
    player, "ui.player_search.title", "ui.player_search.item.confirm", { input ->
        val offlinePlayer = try {
            Bukkit.getOfflinePlayer(input)
        } catch (_: Exception) { null }

        if (onlyConnected)
            onInput(offlinePlayer?.player)
        else
            onInput(offlinePlayer)
    }, returnTo = returnTo
)