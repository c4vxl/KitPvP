package de.c4vxl.kitpvp.ui.editor

import de.c4vxl.gamemanager.language.Language
import de.c4vxl.gamemanager.language.Language.Companion.language
import de.c4vxl.gamemanager.utils.ItemBuilder
import de.c4vxl.kitpvp.data.kit.Kit
import de.c4vxl.kitpvp.data.kit.item.KitItem
import de.c4vxl.kitpvp.handlers.KitEditorHandler
import de.c4vxl.kitpvp.handlers.UIHandler
import de.c4vxl.kitpvp.ui.editor.type.KitEditorItems
import de.c4vxl.kitpvp.ui.type.UI
import de.c4vxl.kitpvp.utils.Item
import de.c4vxl.kitpvp.utils.Item.addMarginItems
import de.c4vxl.kitpvp.utils.Item.guiItem
import de.c4vxl.kitpvp.utils.Item.onDrop
import de.c4vxl.kitpvp.utils.TimeUtils
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.minimessage.MiniMessage
import net.minecraft.world.item.equipment.ArmorType
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory

/**
 * The Kit editor gui
 */
class KitEditor(
    val player: Player,
    var kit: Kit,
    val language: Language = player.language.child("kitpvp"),
    val onDone: (Kit) -> Unit,
    val onClose: () -> Unit
) : UI {
    private val title = language.getCmp("editor.page.main.title", kit.metadata.name)
    private var currentSection = "weapons"

    val baseInventory: Inventory
        get() =
            Bukkit.createInventory(null, 9 * 6, title)
                .apply {
                    addMarginItems(0..8, 10..17, 45..53, 9..36 step 9, 17..45 step 9)

                    // Exit items
                    setItem(0, item(Material.RED_STAINED_GLASS_PANE, "discard").guiItem { discard(it) }.build())
                    setItem(8, item(Material.GREEN_STAINED_GLASS_PANE, "save").guiItem { save(it) }.build())

                    setItem(53, item(Material.COMMAND_BLOCK, "rules").guiItem { KitEditorGameRules(this@KitEditor) }.build())

                    setItem(45, item(Material.BARRIER, "reset").guiItem {
                        if (!(it.isShiftClick && it.isRightClick)) {
                            player.playSound(player.location, Sound.ENTITY_VILLAGER_NO, 3f, 1f)
                            return@guiItem
                        }

                        kit = Kit(kit.metadata)
                        open()
                        player.stopAllSounds()
                        player.playSound(player.location, Sound.BLOCK_GRINDSTONE_USE, 5f, 1f)
                    }.build())

                    // Armor items
                    armorItem(2, this, ArmorType.HELMET, "helmet")
                    armorItem(3, this, ArmorType.CHESTPLATE, "chestplate")
                    armorItem(4, this, ArmorType.LEGGINGS, "leggings")
                    armorItem(5, this, ArmorType.BOOTS, "boots")

                    // Offhand item
                    if (kit.offhand == null)
                        setItem(6, item(Material.ITEM_FRAME, "offhand").guiItem { event ->
                            if (event.action != InventoryAction.SWAP_WITH_CURSOR)
                                return@guiItem

                            kit.offhand = KitItem.fromItem(event.cursor)
                            event.whoClicked.setItemOnCursor(null)
                            open()
                        }.build())

                    else
                        setItem(6, KitEditorItems.editableItem(kit.offhand!!, this@KitEditor)
                            .apply { lore = mutableListOf(
                                Component.empty(),
                                language.getCmp("editor.item.inv.offhand.lore.1") as TextComponent,
                                language.getCmp("editor.item.inv.offhand.lore.2") as TextComponent
                            ) }
                            .guiItem {
                                if (it.isRightClick && it.isShiftClick)
                                    KitEditorEdit(this@KitEditor, kit.offhand!!) { updated ->
                                        kit.offhand = updated
                                    }

                                if (it.action.name.contains("DROP")) {
                                    kit.offhand = null
                                    open()
                                }
                            }
                            .onDrop {
                                kit.offhand = null
                                it.itemDrop.remove()
                                open()
                            }
                            .build())

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

                        loadLower()
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
                            it.whoClicked.setItemOnCursor(KitEditorItems.editableItem(
                                KitItem(it.currentItem!!.type),
                                this@KitEditor
                            ).build())
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

        // Close editor
        UIHandler.nonClosable.remove(player.uniqueId)
        player.closeInventory()
        onClose()

        player.playSound(player.location, Sound.BLOCK_ANVIL_HIT, 3f, 2f)
    }

    private fun save(event: InventoryClickEvent) {
        val player = event.whoClicked as Player

        player.playSound(player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 3f, 2f)

        // Prompt for name
        KitEditorRename.open(player, kit, {
            updateRegistry()

            // Close editor
            player.closeInventory()
            player.inventory.clear()

            UIHandler.nonClosable.remove(player.uniqueId)

            kit.metadata.lastEdit = TimeUtils.now
            onDone(kit)
            onClose()
        }, this)

        player.inventory.clear()
    }

    private fun armorItem(slot: Int, inventory: Inventory, type: ArmorType, armorKey: String) {
        val item = kit.getArmorItem(type)

        if (item == null) {
            inventory.setItem(slot, item(Material.ARMOR_STAND, armorKey).guiItem {
                KitEditorArmor(this@KitEditor, type)
            }.build())
            return
        }

        val builder = item.builder

        inventory.setItem(slot, ItemBuilder(
            builder.material,
            language.getCmp("editor.item.$armorKey.name"),
            builder.amount,
            mutableListOf(
                Component.empty(),
                language.getCmp("editor.item.armor.lore.1") as TextComponent,
                language.getCmp("editor.item.armor.lore.2") as TextComponent
            ),
            builder.unbreakable, builder.enchantments)
            .guiItem { event ->
                if (event.isLeftClick)
                    KitEditorArmor(this@KitEditor, type)

                if (event.isRightClick)
                    KitEditorEdit(this@KitEditor, item) {
                        kit.setArmorPiece(type, it)
                    }
            }
            .build())
    }

    /**
     * Opens an ui for a player
     * @param inv The ui to open
     */
    private fun open(inv: Inventory) {
        loadLower()

        if (player.openInventory.topInventory.size == inv.size && player.openInventory.title() == title) {
            player.openInventory.topInventory.contents = inv.contents
        }
        else
            player.openInventory(inv)

        player.playSound(player.location, Sound.BLOCK_SCAFFOLDING_BREAK, 5f, 0.5f)

        // Add inventory
        updateRegistry()

        UIHandler.nonClosable[player.uniqueId] = this
    }

    /**
     * Loads the players lower inventory
     */
    fun loadLower() {
        player.inventory.clear()
        kit.inventory.toList().forEach { (slot, item) ->
            player.inventory.setItem(slot, KitEditorItems.editableItem(item, this).build())
        }
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

    override fun open() { open(null) }

    /**
     * Updates the opened editor ui inventory
     */
    private fun updateRegistry() {
        KitEditorHandler.openEditors[player.uniqueId] = this
    }

    init {
        open()
    }
}