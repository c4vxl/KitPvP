package de.c4vxl.kitpvp.ui.editor

import de.c4vxl.gamemanager.language.Language
import de.c4vxl.gamemanager.language.Language.Companion.language
import de.c4vxl.gamemanager.utils.ItemBuilder
import de.c4vxl.kitpvp.data.Kit
import de.c4vxl.kitpvp.handlers.KitEditorHandler
import de.c4vxl.kitpvp.ui.editor.type.KitEditorItems
import de.c4vxl.kitpvp.utils.Item
import de.c4vxl.kitpvp.utils.Item.addMarginItems
import de.c4vxl.kitpvp.utils.Item.guiItem
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.minimessage.MiniMessage
import net.minecraft.world.item.equipment.ArmorType
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory

/**
 * The Kit editor gui
 */
class KitEditor(
    val player: Player,
    val kit: Kit,
    val language: Language = player.language.child("kitpvp")
) {
    private val title = language.getCmp("editor.page.main.title", kit.name)
    private var currentSection = "weapons"

    val baseInventory: Inventory
        get() =
            Bukkit.createInventory(null, 9 * 6, title)
                .apply {
                    addMarginItems(0..8, 10..17, 45..53, 9..36 step 9, 17..45 step 9)

                    // Exit items
                    setItem(0, item(Material.RED_STAINED_GLASS_PANE, "discard").guiItem { discard(it) }.build())
                    setItem(8, item(Material.GREEN_STAINED_GLASS_PANE, "save").guiItem { save(it) }.build())

                    // Armor items
                    armorItem(2, this, ArmorType.HELMET, "helmet")
                    armorItem(3, this, ArmorType.CHESTPLATE, "chestplate")
                    armorItem(4, this, ArmorType.LEGGINGS, "leggings")
                    armorItem(5, this, ArmorType.BOOTS, "boots")
                    setItem(6, item(Material.ITEM_FRAME, "offhand").guiItem {  }.build())

                    // Tab items
                    mapOf(
                        "weapons" to Material.DIAMOND_SWORD,
                        "consumables" to Material.COOKED_BEEF,
                        "blocks" to Material.END_STONE,
                        "utils" to Material.DIAMOND
                    ).toList().forEachIndexed { i, (key, material) ->
                        setItem(47 + i, item(material, key, "section").guiItem { open(key) }.build())
                    }

                    setItem(51, item(Material.NAME_TAG, "search", "section").guiItem { KitEditorItemSearch(this@KitEditor) { results ->
                        open(
                            withItems(
                                results.take(30).map { material ->
                                    KitEditorItems.item(material, language)
                                }
                            )
                        )
                    } }.build())
                }

    /**
     * Creates a base gui item
     * @param material The material of the item
     * @param key The name of the item in the translation key
     * @param category The section of the translation key
     */
    private fun item(material: Material, key: String, category: String = "item"): ItemBuilder {
        val lore = language.getCmp("editor.$category.$key.lore").takeIf {
            MiniMessage.miniMessage().serialize(it) != "editor.$category.$key.lore"
        }

        return ItemBuilder(
            material,
            language.getCmp("editor.$category.$key.name"),
            lore = mutableListOf(lore as? TextComponent).filterNotNull().toMutableList()
        )
    }

    /**
     * Returns a base baseInventory and adds a list of items
     * @param items The list of items (more than 18 will be discarded)
     */
    private fun withItems(items: List<ItemBuilder>): Inventory =
        baseInventory.apply {
            val marginItem = Item.marginItem(Material.GRAY_STAINED_GLASS_PANE)

            for (i in 0..21) {
                addItem(
                    items.getOrNull(i)
                        ?.guiItem {
                            it.isCancelled = true
                            it.whoClicked.setItemOnCursor(KitEditorItems.editableItem(it.currentItem!!.type, language))
                        }
                        ?.build()
                        ?: marginItem
                )
            }
        }

    private fun discard(event: InventoryClickEvent) {
        val player = event.whoClicked as Player

        if (!(event.isShiftClick && event.isRightClick)) {
            player.playSound(player.location, Sound.ENTITY_VILLAGER_NO, 3f, 1f)
            return
        }

        player.playSound(player.location, Sound.BLOCK_ANVIL_HIT, 3f, 2f)
    }

    private fun save(event: InventoryClickEvent) {
        val player = event.whoClicked as Player

        player.playSound(player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 3f, 2f)
    }

    private fun armorItem(slot: Int, inventory: Inventory, type: ArmorType, armorKey: String) {
        val item = kit.getArmorItem(type)

        val key = if (item == null) "armor.empty" else armorKey
        val material = item?.material ?: Material.ARMOR_STAND

        inventory.setItem(slot, item(material, key).guiItem {
            KitEditorArmor(this@KitEditor, type)
        }.build())
    }

    /**
     * Opens an ui for a player
     * @param inv The ui to open
     */
    private fun open(inv: Inventory) {
        if (player.openInventory.topInventory.size == inv.size && player.openInventory.title() == title) {
            player.openInventory.topInventory.contents = inv.contents
        }
        else
            player.openInventory(inv)

        player.inventory.clear()
        kit.inventory.forEach { (slot, item) ->
            player.inventory.setItem(slot, item.builder.build())
        }

        // Add inventory
        KitEditorHandler.openEditors[inv] = this
    }

    /**
     * Opens the ui at a specific page/section
     * @param section The section to open
     */
    fun open(section: String? = null) {
        currentSection = section ?: currentSection
        val inv = withItems(KitEditorItems.getItems(player, currentSection))
        open(inv)
    }
}