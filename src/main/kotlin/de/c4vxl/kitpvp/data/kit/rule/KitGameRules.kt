package de.c4vxl.kitpvp.data.kit.rule

import org.bukkit.potion.PotionEffectType

/**
 * Data class holding kit game rules
 */
data class KitGameRules(
    var isAlwaysDay: Boolean = true,
    var isKeepInventory: Boolean = true,
    var isFallDamage: Boolean = true,
    var isItemDrop: Boolean = true,
    var isAllowBlockBreaking: Boolean = true,
    var numRounds: Int = 3,
    var health: Double = 20.0,
    var activeEffects: MutableMap<String, Int> = mutableMapOf()
) {
    var activeEffectsMap
        get() =
            activeEffects.mapNotNull { (k, v) -> ((PotionEffectType.values().find { it.name.lowercase() == k.lowercase() } ?: return@mapNotNull null) to v) }
                .toMap().toMutableMap()
        set(value) {
            activeEffects = value.mapKeys { it.key.name }.toMutableMap()
        }
}