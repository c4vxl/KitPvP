package de.c4vxl.kitpvp.data

import de.c4vxl.gamemanager.utils.ItemBuilder
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

data class KitItem(
    val material: Material,
    val amount: Int = 1,
    val unbreakable: Boolean = false,
    val name: String? = null
) {
    val nameComponent =
        // Load custom name as component
        name?.let { MiniMessage.miniMessage().deserialize(it) }

            // Otherwise use default name
            ?: Component.translatable(material.translationKey())

    val builder: ItemBuilder =
        ItemBuilder(
            material = material,
            name = nameComponent,
            amount = amount,
            unbreakable = unbreakable
        )

    companion object {
        /**
         * Constructs a kit item from an item stack
         * @param item The item stack
         */
        fun fromItem(item: ItemStack?): KitItem? {
            if (item == null)
                return null

            val meta = if (item.hasItemMeta()) item.itemMeta else null

            return KitItem(
                item.type,
                item.amount,
                meta?.isUnbreakable ?: false,
                meta?.displayName()?.let { MiniMessage.miniMessage().serialize(it) }
            )
        }
    }
}