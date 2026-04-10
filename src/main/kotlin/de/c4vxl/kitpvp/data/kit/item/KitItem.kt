package de.c4vxl.kitpvp.data.kit.item

import de.c4vxl.gamemanager.utils.ItemBuilder
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.potion.PotionType
import kotlin.math.max

data class KitItem(
    var material: Material,
    var amount: Int = 1,
    var unbreakable: Boolean = false,
    var name: String? = null,
    var enchantments: MutableMap<String, Int> = mutableMapOf(),
    var effects: MutableMap<String, Pair<Int, Int>> = mutableMapOf()
) {
    val nameComponent: Component get() =
        // Load custom name as component
        name?.let { MiniMessage.miniMessage().deserialize(it) }

            // Otherwise use default name
            ?: Component.translatable(material.translationKey())

    val enchantmentMap get() =
        enchantments.mapKeys { Enchantment.values().find { e -> e.name == it.key } ?: return@mapKeys Enchantment.UNBREAKING }.toMutableMap()

    val effectsMap get() =
        effects.mapNotNull { (PotionEffectType.getByName(it.key) ?: return@mapNotNull null) to it.value }.toMap()

    val itemMeta: ItemMeta get() {
        val meta = ItemStack(material).itemMeta

        (meta as? PotionMeta)?.apply {
            basePotionType = PotionType.WATER
            val amplifier = if (material.name.contains("ARROW")) 8 else 1
            effectsMap.forEach { addCustomEffect(PotionEffect(it.key, it.value.second * 20 * amplifier, max(0, it.value.first - 1)), true) }
        }

        return meta
    }

    val builder: ItemBuilder get() =
        ItemBuilder(
            material = material,
            name = nameComponent,
            amount = amount,
            unbreakable = unbreakable,
            enchantments = enchantmentMap,
            itemMeta = itemMeta
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
                meta?.itemName()?.let { MiniMessage.miniMessage().serialize(it) }?.takeIf { it.isNotBlank() },
                meta?.enchants?.mapKeys { it.key.name }?.toMutableMap() ?: mutableMapOf()
            )
        }
    }
}