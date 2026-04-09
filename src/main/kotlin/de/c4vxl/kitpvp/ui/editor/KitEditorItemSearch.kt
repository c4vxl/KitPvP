package de.c4vxl.kitpvp.ui.editor

import de.c4vxl.kitpvp.handlers.KitEditorHandler
import de.c4vxl.kitpvp.ui.general.AnvilUI
import org.bukkit.Material
import org.bukkit.Sound

/**
 * The Kit editor item search gui
 */
class KitEditorItemSearch(
    editor: KitEditor,
    val result: (List<Material>) -> Unit,
) {
    init {
        AnvilUI(
            editor.player,
            "editor.page.search.title",
            "editor.page.search.item.confirm",
            {
                result(query(it))
            }
        )

        editor.player.inventory.clear()
        editor.player.playSound(editor.player.location, Sound.BLOCK_SCAFFOLDING_BREAK, 5f, 0.5f)
    }

    /**
     * Queries all items for a certain query
     * @param query The search query
     */
    private fun query(query: String): List<Material> {
        val lowerQuery = query.lowercase()
            .replace(" ", "_")

        return Material.entries
            // Exact matches first
            .sortedWith(compareByDescending {
                it.name.lowercase() == lowerQuery
            })

            // Similar matches
            .filter { it.name.lowercase().contains(lowerQuery) }

            // Filter out invalid materials
            .filter { it.isItem }
    }
}